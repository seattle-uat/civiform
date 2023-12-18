package views.fileupload;

import static com.google.common.base.Preconditions.checkNotNull;
import static j2html.TagCreator.input;

import com.google.common.collect.ImmutableList;
import j2html.tags.Tag;
import j2html.tags.specialized.InputTag;
import j2html.tags.specialized.ScriptTag;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import services.cloud.StorageUploadRequest;
import services.cloud.azure.BlobStorageUploadRequest;
import views.ViewUtils;

public final class AzureFileUploadViewStrategy extends FileUploadViewStrategy {

  private final ViewUtils viewUtils;

  @Inject
  AzureFileUploadViewStrategy(ViewUtils viewUtils) {
    this.viewUtils = checkNotNull(viewUtils);
  }

  @Override
  public ImmutableList<Tag<?>> fileUploadFormInputs(
      Optional<StorageUploadRequest> request,
      String acceptedMimeTypes,
      String fileInputId,
      ImmutableList<String> ariaDescribedByIds,
      boolean hasErrors) {
    if (request.isEmpty()) {
      return ImmutableList.of();
    }
    BlobStorageUploadRequest signedRequest = castStorageRequest(request.get());
    ImmutableList.Builder<Tag<?>> builder = ImmutableList.builder();
    builder.add(
        input().withType("hidden").withName("fileName").withValue(signedRequest.fileName()),
        input().withType("hidden").withName("sasToken").withValue(signedRequest.sasToken()),
        input().withType("hidden").withName("blobUrl").withValue(signedRequest.blobUrl()),
        input()
            .withType("hidden")
            .withName("containerName")
            .withValue(signedRequest.containerName()),
        input().withType("hidden").withName("accountName").withValue(signedRequest.accountName()),
        input()
            .withType("hidden")
            .withName("successActionRedirect")
            .withValue(signedRequest.successActionRedirect()),
        // TODO: USWDS here also
        input()
            .withId(fileInputId)
            .condAttr(hasErrors, "aria-invalid", "true")
            .condAttr(
                !ariaDescribedByIds.isEmpty(),
                "aria-describedby",
                StringUtils.join(ariaDescribedByIds, " "))
            .withType("file")
            .withName("file")
            .withClass("hidden")
            .withAccept(acceptedMimeTypes));
    return builder.build();
  }

  private BlobStorageUploadRequest castStorageRequest(StorageUploadRequest request) {
    if (!(request instanceof BlobStorageUploadRequest)) {
      throw new RuntimeException(
          "Tried to upload a file to Azure Blob storage using incorrect request type");
    }
    return (BlobStorageUploadRequest) request;
  }

  @Override
  public ImmutableList<ScriptTag> extraScriptTags() {
    return ImmutableList.of(viewUtils.makeAzureBlobStoreScriptTag());
  }

  @Override
  protected String getUploadFormClass() {
    return "azure-upload";
  }
}
