package org.gbif.pipelines.transforms.table;

import com.google.common.reflect.Reflection;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.gbif.pipelines.common.PipelinesVariables;
import org.gbif.pipelines.common.beam.options.InterpretationPipelineOptions;
import org.gbif.pipelines.core.config.model.DataWarehouseConfig;
import org.gbif.pipelines.core.config.model.PipelinesConfig;
import org.gbif.pipelines.core.factory.FileSystemFactory;
import org.gbif.pipelines.core.pojo.HdfsConfigs;
import org.gbif.pipelines.core.utils.FsUtils;

@Data
@Slf4j
public class PartitionedTableTransform {

  private static final String EXT_PACKAGE = "org.gbif.pipelines.io.avro.extension";

  private PipelinesVariables.Pipeline.Interpretation.RecordType recordType;

  private final Schema schema;

  private final HdfsConfigs hdfsConfigs;

  private final PipelinesConfig config;

  private final InterpretationPipelineOptions options;

  @Builder
  public PartitionedTableTransform(
      Schema schema,
      @Nullable PipelinesVariables.Pipeline.Interpretation.RecordType recordType,
      InterpretationPipelineOptions options) {
    this.recordType = recordType;
    this.schema = schema;
    this.hdfsConfigs = HdfsConfigs.create(options.getHdfsSiteConfig(), options.getCoreSiteConfig());
    this.config =
        FsUtils.readConfigFile(hdfsConfigs, options.getProperties(), PipelinesConfig.class);
    this.options = options;
  }

  private static HiveClient getHiveClient(DataWarehouseConfig config) {
    return HiveClient.builder()
        .connectionString(config.getConnectionString())
        .hiveUsername(config.getUsername())
        .hivePassword(config.getPassword())
        .build();
  }

  /**
   * Creates the target where a new partition would be created. A timestamp is appended to guarantee
   * uniqueness.
   */
  private String getPartitionTargetPath() {
    return config.getDataWarehouseConfig().getExternalStorePath()
        + getTableDirectoryName()
        + "/datasetkey="
        + options.getDatasetId()
        + '_'
        + new Date().getTime();
  }

  /**
   * Creates a glob patterns to list all directories and files of dataset in the table partitions
   * directory.
   */
  private static String getPartitionSourcePattern(
      InterpretationPipelineOptions options,
      PipelinesVariables.Pipeline.Interpretation.RecordType recordType) {
    return options.getTargetPath()
        + '/'
        + options.getCoreRecordType().name().toLowerCase()
        + '/'
        + recordType.name().toLowerCase().replace("_", "")
        + '/'
        + options.getDatasetId()
        + '_'
        + options.getAttempt()
        + "*.parquet";
  }

  public static void addOrUpdatePartition(InterpretationPipelineOptions options, Schema schema) {
    addOrUpdatePartition(options, schema, null);
  }

  public static void addOrUpdatePartition(
      InterpretationPipelineOptions options,
      Schema schema,
      PipelinesVariables.Pipeline.Interpretation.RecordType extension) {

    PartitionedTableTransform.builder()
        .recordType(extension)
        .options(options)
        .schema(schema)
        .build()
        .addOrUpdatePartition();
  }

  private String getTableName() {
    if (recordType == null) {
      return options.getCoreRecordType().name().toLowerCase();
    }
    return getExtensionTableName();
  }

  private String getTableDirectoryName() {
    if (recordType == null) {
      return options.getCoreRecordType().name().toLowerCase();
    }
    String className = schema.getFullName();
    String packageName = Reflection.getPackageName(className);
    String simpleClassName = className.substring(packageName.length() + 1).toLowerCase();
    String leafNamespace = packageName.replace(EXT_PACKAGE + '.', "").replace('.', '_');
    return leafNamespace + '_' + simpleClassName.replace("table", "");
  }

  private String getExtensionTableName() {
    return options.getCoreRecordType().name().toLowerCase() + "_ext_" + getTableDirectoryName();
  }

  private void copyDataFiles(String targetPartitionPath) {
    PipelinesVariables.Pipeline.Interpretation.RecordType targetRecordType =
        Optional.ofNullable(recordType).orElse(options.getCoreRecordType());

    String globPattern = getPartitionSourcePattern(options, targetRecordType);

    log.info("Copying files from [{}], to [{}]", globPattern, targetPartitionPath);
    FsUtils.copyDirectory(hdfsConfigs, targetPartitionPath, globPattern);
  }

  public void addOrUpdatePartition() {
    String targetPartitionPath = getPartitionTargetPath();
    copyDataFiles(targetPartitionPath);
    getHiveClient(config.getDataWarehouseConfig())
        .addOrAlterPartition(
            getTableName(), "datasetkey", options.getDatasetId(), targetPartitionPath);
    deletePreviousDatasetPartition(targetPartitionPath);
  }

  @SneakyThrows
  private void deletePreviousDatasetPartition(String location) {
    FileSystem fs = FileSystemFactory.getInstance(hdfsConfigs).getHdfsFs();
    Path targetPath = new Path(location + '/');
    String prefix = "datasetkey=" + options.getDatasetId();
    FsUtils.deleteDirectoriesByPrefixAllExcept(
        fs, targetPath.getParent(), prefix, targetPath.getName());
  }
}
