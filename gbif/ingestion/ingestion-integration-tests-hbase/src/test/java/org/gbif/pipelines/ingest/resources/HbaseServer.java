package org.gbif.pipelines.ingest.resources;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.TestingServer;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.gbif.pipelines.core.config.model.PipelinesConfig;
import org.gbif.pipelines.fragmenter.common.HbaseStore;
import org.gbif.pipelines.keygen.config.KeygenConfig;
import org.junit.rules.ExternalResource;

@Slf4j
@Getter
public class HbaseServer extends ExternalResource {

  public static final String PROPERTIES_PATH = "data7/ingest/pipelines.yaml";

  public static final KeygenConfig CFG =
      KeygenConfig.builder()
          .counterTable("test_occurrence_counter")
          .lookupTable("test_occurrence_lookup")
          .occurrenceTable("test_occurrence")
          .zkConnectionString(null)
          .create();

  public static final String FRAGMENT_TABLE_NAME = "fragment_table";
  public static final byte[] FRAGMENT_TABLE = Bytes.toBytes(FRAGMENT_TABLE_NAME);

  private static final byte[] LOOKUP_TABLE = Bytes.toBytes(CFG.getLookupTable());
  private static final String CF_NAME = "o";
  private static final byte[] CF = Bytes.toBytes(CF_NAME);
  private static final byte[] COUNTER_TABLE = Bytes.toBytes(CFG.getCounterTable());
  private static final String COUNTER_CF_NAME = "o";
  private static final byte[] COUNTER_CF = Bytes.toBytes(COUNTER_CF_NAME);
  private static final byte[] OCCURRENCE_TABLE = Bytes.toBytes(CFG.getOccurrenceTable());

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();

  private Connection connection = null;
  private TestingServer zkServer;

  public void truncateTable() throws IOException {
    log.info("Trancate the table");
    TEST_UTIL.truncateTable(FRAGMENT_TABLE);
  }

  @Override
  protected void before() throws Throwable {

    log.info("Create hbase mini-cluster");
    zkServer = new TestingServer(true);
    CFG.setZkConnectionString(zkServer.getConnectString());
    TEST_UTIL.getConfiguration().setInt("hbase.master.port", HBaseTestingUtility.randomFreePort());
    TEST_UTIL
        .getConfiguration()
        .setInt("hbase.master.info.port", HBaseTestingUtility.randomFreePort());
    TEST_UTIL
        .getConfiguration()
        .setInt("hbase.regionserver.port", HBaseTestingUtility.randomFreePort());
    TEST_UTIL
        .getConfiguration()
        .setInt("hbase.regionserver.info.port", HBaseTestingUtility.randomFreePort());
    TEST_UTIL.getConfiguration().setStrings("hbase.zookeeper.quorum", zkServer.getConnectString());
    TEST_UTIL.startMiniCluster(1);

    TEST_UTIL.createTable(FRAGMENT_TABLE, HbaseStore.getFragmentFamily());
    TEST_UTIL.createTable(LOOKUP_TABLE, CF);
    TEST_UTIL.createTable(COUNTER_TABLE, COUNTER_CF);
    TEST_UTIL.createTable(OCCURRENCE_TABLE, CF);

    connection = ConnectionFactory.createConnection(TEST_UTIL.getConfiguration());

    updateZkProperties();
  }

  @SneakyThrows
  @Override
  protected void after() {
    log.info("Shut down hbase mini-cluster");
    TEST_UTIL.shutdownMiniCluster();
    if (connection != null) {
      connection.close();
    }
    if (zkServer != null) {
      zkServer.stop();
      zkServer.close();
    }
  }

  private void updateZkProperties() throws IOException, URISyntaxException {
    // create props
    PipelinesConfig config;
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER));
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    mapper.findAndRegisterModules();

    File resource =
        Paths.get(
                Thread.currentThread().getContextClassLoader().getResource(PROPERTIES_PATH).toURI())
            .toFile();
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
      config = mapper.readValue(in, PipelinesConfig.class);
      config.setZkConnectionString(TEST_UTIL.getZooKeeperWatcher().getQuorum());
    }

    // write properties to the file
    try (FileOutputStream out = new FileOutputStream(resource)) {
      mapper.writeValue(out, config);
    }
  }
}
