package services.cloud.aws;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.typesafe.config.Config;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mockito.Mockito;
import play.Environment;
import play.inject.ApplicationLifecycle;
import services.cloud.ApplicantStorageClient;
import services.cloud.StorageServiceName;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/** An AWS Simple Storage Service (S3) implementation of {@link ApplicantStorageClient}. */
@Singleton
public class ApplicantSimpleStorage implements ApplicantStorageClient {

  public static final String AWS_S3_BUCKET_CONF_PATH = "aws.s3.bucket";
  public static final Duration AWS_PRESIGNED_URL_DURATION = Duration.ofMinutes(10);
  public static final String AWS_S3_FILE_LIMIT_CONF_PATH = "aws.s3.filelimitmb";

  private final Region region;
  private final Credentials credentials;
  private final String bucket;
  private final int fileLimitMb;
  private final Client client;

  @Inject
  public ApplicantSimpleStorage(
      AwsRegion region,
      Credentials credentials,
      Config config,
      Environment environment,
      ApplicationLifecycle appLifecycle) {
    this.region = checkNotNull(region).get();
    this.credentials = checkNotNull(credentials);
    this.bucket = checkNotNull(config).getString(AWS_S3_BUCKET_CONF_PATH);
    this.fileLimitMb = checkNotNull(config).getInt(AWS_S3_FILE_LIMIT_CONF_PATH);
    if (environment.isDev()) {
      client = new LocalStackClient(config);
    } else if (environment.isTest()) {
      client = new NullClient();
    } else {
      client = new AwsClient();
    }

    appLifecycle.addStopHook(
        () -> {
          client.close();
          return CompletableFuture.completedFuture(null);
        });
  }

  @Override
  public String getPresignedUrlString(String fileKey) {
    // TODO(#1841): support storing and displaying original filenames for AWS uploads
    return getPresignedUrlString(fileKey, /* originalFileName= */ Optional.empty());
  }

  @Override
  public String getPresignedUrlString(String fileKey, Optional<String> originalFileName) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().key(fileKey).bucket(bucket).build();
    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(AWS_PRESIGNED_URL_DURATION)
            .getObjectRequest(getObjectRequest)
            .build();

    PresignedGetObjectRequest presignedGetObjectRequest =
        client.getPresigner().presignGetObject(getObjectPresignRequest);
    return presignedGetObjectRequest.url().toString();
  }

  @Override
  public SignedS3UploadRequest getSignedUploadRequest(String key, String successActionRedirect) {
    AwsCredentials awsCredentials = credentials.getCredentials();
    SignedS3UploadRequest.Builder builder =
        SignedS3UploadRequest.builder()
            .setActionLink(client.bucketAddress())
            .setKey(key)
            .setSuccessActionRedirect(successActionRedirect)
            .setAccessKey(awsCredentials.accessKeyId())
            .setExpirationDuration(AWS_PRESIGNED_URL_DURATION)
            .setBucket(bucket)
            .setSecretKey(awsCredentials.secretAccessKey())
            .setRegionName(region.id())
            .setFileLimitMb(fileLimitMb);
    if (awsCredentials instanceof AwsSessionCredentials) {
      AwsSessionCredentials sessionCredentials = (AwsSessionCredentials) awsCredentials;
      builder.setSecurityToken(sessionCredentials.sessionToken());
    }
    return builder.build();
  }

  @Override
  public StorageServiceName getStorageServiceName() {
    return StorageServiceName.AWS_S3;
  }

  interface Client {

    S3Presigner getPresigner();

    String bucketAddress();

    void close();
  }

  static class NullClient implements Client {

    private final S3Presigner presigner;

    NullClient() {
      presigner = Mockito.mock(S3Presigner.class);
      PresignedGetObjectRequest presignedGetObjectRequest =
          Mockito.mock(PresignedGetObjectRequest.class);
      URL fakeUrl;
      try {
        fakeUrl = new URL("http://fake-url");
      } catch (java.net.MalformedURLException e) {
        throw new RuntimeException(e);
      }
      when(presignedGetObjectRequest.url()).thenReturn(fakeUrl);
      when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
          .thenReturn(presignedGetObjectRequest);
    }

    @Override
    public S3Presigner getPresigner() {
      return presigner;
    }

    @Override
    public String bucketAddress() {
      return "fake-bucket-address";
    }

    @Override
    public void close() {}
  }

  class AwsClient implements Client {

    private final S3Presigner presigner;

    AwsClient() {
      presigner = S3Presigner.builder().region(region).build();
    }

    @Override
    public S3Presigner getPresigner() {
      return presigner;
    }

    @Override
    public String bucketAddress() {
      return String.format("https://%s.s3.%s.amazonaws.com/", bucket, region.id());
    }

    @Override
    public void close() {
      presigner.close();
    }
  }

  class LocalStackClient implements Client {

    private static final String AWS_LOCAL_ENDPOINT_CONF_PATH = "aws.local.endpoint";

    private final String localEndpoint;
    private final String localS3Endpoint;
    private final S3Presigner presigner;

    LocalStackClient(Config config) {
      localEndpoint = checkNotNull(config).getString(AWS_LOCAL_ENDPOINT_CONF_PATH);
      URI localS3Uri;
      try {
        URI localUri = new URI(localEndpoint);
        localS3Endpoint =
            String.format("%s://s3.%s", localUri.getScheme(), localUri.getAuthority());
        localS3Uri = new URI(localS3Endpoint);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
      presigner = S3Presigner.builder().endpointOverride(localS3Uri).region(region).build();
    }

    @Override
    public S3Presigner getPresigner() {
      return presigner;
    }

    @Override
    public String bucketAddress() {
      try {
        return S3EndpointProvider.defaultProvider()
            .resolveEndpoint(
                (builder) -> builder.endpoint(localS3Endpoint).bucket(bucket).region(region))
            .get()
            .url()
            .toString();
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void close() {
      presigner.close();
    }
  }
}
