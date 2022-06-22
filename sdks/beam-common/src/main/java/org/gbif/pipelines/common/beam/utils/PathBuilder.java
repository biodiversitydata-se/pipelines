package org.gbif.pipelines.common.beam.utils;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.Path;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.pipelines.common.PipelinesVariables;
import org.gbif.pipelines.common.beam.options.BasePipelineOptions;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathBuilder {

  /** Build a {@link Path} from an array of string values using path separator. */
  public static Path buildPath(String... values) {
    return new Path(String.join(Path.SEPARATOR, values));
  }

  /**
   * Uses pattern for path - "{targetPath}/{datasetId}/{attempt}/{name}"
   *
   * @return string path
   */
  public static String buildDatasetAttemptPath(
      BasePipelineOptions options, String name, boolean isInput) {
    return String.join(
        Path.SEPARATOR,
        isInput ? options.getInputPath() : options.getTargetPath(),
        options.getDatasetId() == null || "all".equalsIgnoreCase(options.getDatasetId())
            ? "*"
            : options.getDatasetId(),
        options.getAttempt().toString(),
        name.toLowerCase());
  }

  /**
   * Uses pattern for path -
   * "{targetPath}/{datasetId}/{attempt}/{coreTerm}/{name}/interpret-{uniqueId}" The core term path
   * is empty if it is an occurrence.
   *
   * @return string path to interpretation
   */
  public static String buildPathInterpretUsingTargetPath(
      BasePipelineOptions options, DwcTerm core, String name, String uniqueId) {
    return String.join(Path.SEPARATOR,
            buildDatasetAttemptPath(options, core.simpleName().toLowerCase(), false),
            name,
            PipelinesVariables.Pipeline.Interpretation.FILE_NAME + uniqueId);
  }

  /**
   * Uses pattern for path - "{targetPath}/{datasetId}/{attempt}/{core}/{name}/interpret-{uniqueId}"
   *
   * @return string path to interpretation
   */
  public static String buildPathInterpretUsingInputPath(
      BasePipelineOptions options, DwcTerm core, String name, String uniqueId) {
    return String.join(Path.SEPARATOR,
            buildDatasetAttemptPath(options, core.simpleName().toLowerCase(), true),
            name,
            PipelinesVariables.Pipeline.Interpretation.FILE_NAME + uniqueId);
  }

  /**
   * Builds the target base path of a hdfs view.
   *
   * @param options options pipeline options
   * @return path to the directory where the occurrence hdfs view is stored
   */
  public static String buildFilePathViewUsingInputPath(
      BasePipelineOptions options, String type, String uniqueId) {
    String corePath = DwcTerm.Occurrence.simpleName().toLowerCase();
    return buildPath(buildDatasetAttemptPath(options, corePath, true), type.toLowerCase(), uniqueId)
        .toString();
  }

  /**
   * Gets temporary directory from options or returns default value.
   *
   * @return path to a temporary directory.
   */
  public static String getTempDir(BasePipelineOptions options) {
    return Strings.isNullOrEmpty(options.getTempLocation())
        ? buildPath(options.getTargetPath(), "tmp").toString()
        : options.getTempLocation();
  }
}
