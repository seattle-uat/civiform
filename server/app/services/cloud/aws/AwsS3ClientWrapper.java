package services.cloud.aws;

import java.net.URI;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

/**
 * A wrapper around AWS's {@link software.amazon.awssdk.services.s3.S3Client} so that we can fake it
 * out in tests.
 */
public interface AwsS3ClientWrapper {
  /**
   * Deletes an object from AWS S3 cloud storage. The object to delete is specified in the {@code
   * request} and must include both the bucket and the key.
   */
  void deleteObject(
      Credentials credentials, Region region, URI endpoint, DeleteObjectRequest request);
}
