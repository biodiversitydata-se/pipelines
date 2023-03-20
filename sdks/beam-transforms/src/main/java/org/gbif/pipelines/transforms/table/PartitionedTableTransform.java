package org.gbif.pipelines.transforms.table;

import com.google.common.reflect.Reflection;
import java.util.Date;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
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
import org.gbif.pipelines.io.avro.OccurrenceHdfsRecord;

@Data
public class PartitionedTableTransform {

  private static final String EXT_PACKAGE = "org.gbif.pipelines.io.avro.extension";

  private final String tableName;

  private final String tableDirectoryName;

  private final HiveClient hiveClient;

  private final HdfsConfigs hdfsConfigs;

  private final String targetDir;

  @Builder
  public PartitionedTableTransform(
      PipelinesVariables.Pipeline.Interpretation.RecordType recordType,
      Schema schema,
      HiveClient hiveClient,
      HdfsConfigs hdfsConfigs,
      String targetDir) {
    tableName = tableNameFromClass(recordType, schema.getFullName());
    tableDirectoryName = getTableDirectoryName(schema.getFullName());
    this.hiveClient = hiveClient;
    this.hdfsConfigs = hdfsConfigs;
    this.targetDir = targetDir;
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
  private static String getPartitionTargetPath(
      InterpretationPipelineOptions options, PipelinesConfig config) {
    return config.getDataWarehouseConfig().getExternalStorePath()
        + options.getCoreRecordType().name().toLowerCase()
        + "/datasetkey="
        + options.getDatasetId()
        + '_'
        + new Date().getTime();
  }

  /**
   * Creates a glob patterns to list all directories and files of dataset in the table partitions
   * directory.
   */
  private static String getDatasetPartitionPaths(InterpretationPipelineOptions options) {
    return options.getTargetPath()
        + '/'
        + options.getCoreRecordType().name().toLowerCase()
        + '/'
        + options.getCoreRecordType().name().toLowerCase()
        + '/'
        + options.getDatasetId()
        + '_'
        + options.getAttempt()
        + "*.parquet";
  }

  public static void addOrUpdatePartition(InterpretationPipelineOptions options, Schema schema) {

    HdfsConfigs hdfsConfigs =
        HdfsConfigs.create(options.getHdfsSiteConfig(), options.getCoreSiteConfig());

    PipelinesConfig config =
        FsUtils.readConfigFile(hdfsConfigs, options.getProperties(), PipelinesConfig.class);

    String targetPartitionPath = getPartitionTargetPath(options, config);

    String globPattern = getDatasetPartitionPaths(options);

    FsUtils.copyDirectory(hdfsConfigs, targetPartitionPath, globPattern);

    PartitionedTableTransform.builder()
        .recordType(options.getCoreRecordType())
        .hiveClient(getHiveClient(config.getDataWarehouseConfig()))
        .hdfsConfigs(HdfsConfigs.create(options.getHdfsSiteConfig(), options.getCoreSiteConfig()))
        .schema(schema)
        .targetDir(config.getDataWarehouseConfig().getExternalStorePath())
        .build()
        .addOrUpdatePartition(options.getDatasetId(), targetPartitionPath);
  }

  private String tableNameFromClass(
      PipelinesVariables.Pipeline.Interpretation.RecordType recordType, String className) {
    if ((PipelinesVariables.Pipeline.Interpretation.RecordType.OCCURRENCE == recordType
            || PipelinesVariables.Pipeline.Interpretation.RecordType.EVENT == recordType)
        && OccurrenceHdfsRecord.class.getName().equals(className)) {
      return recordType.name().toLowerCase();
    }
    String packageName = Reflection.getPackageName(className);
    String simpleClassName = className.substring(packageName.length() + 1).toLowerCase();
    String leafNamespace = packageName.replace(EXT_PACKAGE + '.', "").replace('.', '_');
    return recordType.name().toLowerCase()
        + '_'
        + leafNamespace
        + '_'
        + simpleClassName.replace("table", "");
  }

  private static String getTableDirectoryName(String className) {
    String packageName = Reflection.getPackageName(className);
    return className.substring(packageName.length() + 1).toLowerCase();
  }

  public void addOrUpdatePartition(String datasetKey, String location) {
    hiveClient.addOrAlterPartition(tableName, "datasetkey", datasetKey, location);
    deletePreviousDatasetPartition(datasetKey, location);
  }

  @SneakyThrows
  private void deletePreviousDatasetPartition(String datasetKey, String location) {
    FileSystem fs = FileSystemFactory.getInstance(hdfsConfigs).getHdfsFs();
    Path targetPath = new Path(location);
    Path globPattern =
        Path.mergePaths(targetPath.getParent(), new Path("/datasetkey=" + datasetKey + "*"));
    FsUtils.deleteAllExcept(fs, globPattern, targetPath);
  }
}
