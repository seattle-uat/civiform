package services.program;

import services.Path;

public class PathNotInBlockException extends Exception {

  public PathNotInBlockException(String blockId, Path path) {
    super(String.format("Screen (ID %s) does not contain path %s", blockId, path));
  }
}
