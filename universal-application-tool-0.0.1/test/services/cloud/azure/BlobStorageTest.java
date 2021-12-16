package services.cloud.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;

import com.google.common.collect.ImmutableMap;
import java.net.URL;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.test.WithApplication;
import services.cloud.StorageServiceName;
import services.cloud.azure.BlobStorage.Client;
import services.cloud.azure.BlobStorage.NullClient;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

public class BlobStorageTest extends WithApplication {

  private static final String TEST_FILE_NAME = "fileName";
  private static final ImmutableMap<String, Object> TEST_AZURE_CONTAINER_CONFIG =
      ImmutableMap.of(
          "azure.blob.container",
          "super cool blob container name",
          "azure.blob.account",
          "my awesome azure account name");
  private BlobStorage blobStorage;

  @Override
  protected Application provideApplication() {
    return fakeApplication(TEST_AZURE_CONTAINER_CONFIG);
  }

  @Before
  public void setUp() {
    this.blobStorage = instanceOf(BlobStorage.class);
  }

  @Test
  public void getClient_instanceOfNullClient() {
    Client client = blobStorage.getClient();

    assertThat(client).isInstanceOf(NullClient.class);
    assertThat(client).isInstanceOf(Client.class);
  }

  @Test
  public void getBlobUrl() {
    String blobUrl = blobStorage.getClient().getBlobUrl(TEST_FILE_NAME);

    assertThat(blobUrl).isEqualTo("http://www.blobUrl.com");
  }

  @Test
  public void getSasToken() {
    String sasToken = blobStorage.getClient().getSasToken(TEST_FILE_NAME);

    assertThat(sasToken).isEqualTo("sasToken");
  }

  @Test
  public void getPresignedUrl() {
    URL url = blobStorage.getPresignedUrl(TEST_FILE_NAME);

    assertThat(url.toString()).isEqualTo("http://www.blobUrl.com?sasToken");
  }

  @Test
  public void getStorageServiceName() {
    StorageServiceName storageServiceName = blobStorage.getStorageServiceName();

    assertThat(storageServiceName).isEqualTo(StorageServiceName.AZURE_BLOB);
  }

  @Test
  public void getFileUploadRequest() {
    BlobStorageUploadRequest expectedRequest =
        BlobStorageUploadRequest.builder()
            .setBlobUrl("http://www.blobUrl.com")
            .setAccountName("my awesome azure account name")
            .setFileName(TEST_FILE_NAME)
            .setContainerName("super cool blob container name")
            .setSasToken("sasToken")
            .setSuccessActionRedirect("www.redirectlink.com")
            .setServiceName(StorageServiceName.AZURE_BLOB.getString())
            .build();

    BlobStorageUploadRequest blobStorageUploadRequest =
        blobStorage.getSignedUploadRequest(
            TEST_FILE_NAME, /* successActionRedirect= */ "www.redirectlink.com");

    assertThat(blobStorageUploadRequest).isEqualTo(expectedRequest);
  }
}
