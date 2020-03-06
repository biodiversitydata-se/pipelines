package org.gbif.pipelines.fragmenter.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.gbif.pipelines.fragmenter.common.HbaseStore.getAttemptQualifier;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getDatasetIdQualifier;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getDateCreatedQualifier;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getDateUpdatedQualifier;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getFragmentFamily;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getProtocolQualifier;
import static org.gbif.pipelines.fragmenter.common.HbaseStore.getRecordQualifier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableAssert {

  public static void assertTablDateUpdated(Connection connection, int expectedSize, String expectedDatasetId,
      Integer expectedAttempt, String expectedProtocol) throws IOException {
    assertTable(connection, expectedSize, expectedDatasetId, expectedAttempt, expectedProtocol, true);
  }

  public static void assertTable(Connection connection, int expectedSize, String expectedDatasetId,
      Integer expectedAttempt, String expectedProtocol) throws IOException {
    assertTable(connection, expectedSize, expectedDatasetId, expectedAttempt, expectedProtocol, false);
  }

  private static void assertTable(Connection connection, int expectedSize, String expectedDatasetId,
      Integer expectedAttempt, String expectedProtocol, boolean useDateUpdated) throws IOException {
    TableName tableName = TableName.valueOf(HbaseServer.FRAGMENT_TABLE_NAME);
    try (Table table = connection.getTable(tableName);
        ResultScanner rs = table.getScanner(getFragmentFamily())) {
      Iterator<Result> iterator = rs.iterator();
      int counter = 0;
      while (iterator.hasNext()) {
        Result r = iterator.next();

        byte[] datasetValue = r.getValue(getFragmentFamily(), getDatasetIdQualifier());
        ByteBuffer attemptValue = ByteBuffer.wrap(r.getValue(getFragmentFamily(), getAttemptQualifier()));
        byte[] protocolValue = r.getValue(getFragmentFamily(), getProtocolQualifier());
        byte[] recordValue = r.getValue(getFragmentFamily(), getRecordQualifier());
        ByteBuffer createdValue = ByteBuffer.wrap(r.getValue(getFragmentFamily(), getDateCreatedQualifier()));
        ByteBuffer updatedValue = ByteBuffer.wrap(r.getValue(getFragmentFamily(), getDateUpdatedQualifier()));

        String datasetString = new String(datasetValue);
        Integer attemptInt = attemptValue.getInt();
        String protocolString = new String(protocolValue);
        String recordString = new String(recordValue);
        long createdLong = createdValue.getLong();
        long updatedLong = updatedValue.getLong();

        Assert.assertEquals(expectedDatasetId, datasetString);
        Assert.assertEquals(expectedAttempt, attemptInt);
        Assert.assertEquals(expectedProtocol, protocolString);
        Assert.assertNotNull(recordString);
        Assert.assertTrue(recordString.length() > 0);

        if (useDateUpdated) {
          Assert.assertTrue(updatedLong > createdLong);
        } else {
          Assert.assertEquals(updatedLong, createdLong);
        }

        counter++;
      }
      Assert.assertEquals(expectedSize, counter);
    }
  }

}