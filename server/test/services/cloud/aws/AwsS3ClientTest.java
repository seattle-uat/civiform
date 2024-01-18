package services.cloud.aws;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import org.junit.Test;
import repository.ResetPostgres;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

public class AwsS3ClientTest extends ResetPostgres {
  private static final URI endpointUri = URI.create("https://s3.us-east-2.amazonaws.com");
  private final Credentials credentials = instanceOf(Credentials.class);
  private final AwsS3Client awsS3Client = new AwsS3Client();

  @Test
  public void deleteObjects_noObjectsInRequest_throws() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                awsS3Client.deleteObjects(
                    credentials,
                    Region.US_EAST_2,
                    endpointUri,
                    DeleteObjectsRequest.builder()
                        .bucket("fakeBucket")
                        .delete(Delete.builder().build())
                        .build()))
        .withMessageContaining("must have at least one object");
  }

  @Test
  public void deleteObjects_noBucketInRequest_throws() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                awsS3Client.deleteObjects(
                    credentials,
                    Region.US_EAST_2,
                    endpointUri,
                    DeleteObjectsRequest.builder()
                        .bucket("")
                        .delete(
                            Delete.builder()
                                .objects(ObjectIdentifier.builder().key("key").build())
                                .build())
                        .build()))
        .withMessageContaining("must have a bucket");
  }
}
