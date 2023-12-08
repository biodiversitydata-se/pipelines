package org.gbif.pipelines.common.interpretation;

import static org.junit.Assert.*;

import org.gbif.pipelines.common.configs.SparkConfiguration;
import org.gbif.pipelines.common.process.SparkSettings;
import org.junit.BeforeClass;
import org.junit.Test;

public class SparkSettingsTest {

  private static final SparkConfiguration CONFIG = new SparkConfiguration();

  @BeforeClass
  public static void setUp() {
    CONFIG.recordsPerThread = 250_000;
    CONFIG.parallelismMin = 8;
    CONFIG.parallelismMax = 750;
    CONFIG.memoryOverhead = 6144;
    CONFIG.executorMemoryGbMin = 8;
    CONFIG.executorMemoryGbMax = 70;
    CONFIG.executorCores = 5;
    CONFIG.executorNumbersMin = 2;
    CONFIG.executorNumbersMax = 70;
    CONFIG.driverMemory = "1Gb";
    // Power function setting
    CONFIG.powerFnCoefficient = 0.000138d;
    CONFIG.powerFnExponent = 0.626d;
    CONFIG.powerFnMemoryCoef = 2.8d;
    CONFIG.powerFnExecutorCoefficient = 1d;
    CONFIG.powerFnParallelismCoef = 10d;
    CONFIG.memoryExtraCoef = 1.3d;
  }

  @Test
  public void r100RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 100;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(CONFIG.parallelismMin, sparkSettings.getParallelism());
    assertEquals(CONFIG.executorNumbersMin, sparkSettings.getExecutorNumbers());
    assertEquals(CONFIG.executorMemoryGbMin + "G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m1RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 1_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(CONFIG.parallelismMin, sparkSettings.getParallelism());
    assertEquals(CONFIG.executorNumbersMin, sparkSettings.getExecutorNumbers());
    assertEquals(CONFIG.executorMemoryGbMin + "G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m10RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 10_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(34, sparkSettings.getParallelism());
    assertEquals(4, sparkSettings.getExecutorNumbers());
    assertEquals("10G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m10RecordsExtraMemorySettingsTest() {

    // State
    long fileRecordsNumber = 10_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, true);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(34, sparkSettings.getParallelism());
    assertEquals(4, sparkSettings.getExecutorNumbers());
    assertEquals("13G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m30RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 30_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(68, sparkSettings.getParallelism());
    assertEquals(7, sparkSettings.getExecutorNumbers());
    assertEquals("19G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m100RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 100_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(142, sparkSettings.getParallelism());
    assertEquals(15, sparkSettings.getExecutorNumbers());
    assertEquals("40G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m300RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 300_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(280, sparkSettings.getParallelism());
    assertEquals(28, sparkSettings.getExecutorNumbers());
    assertEquals("70G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void m500RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 500_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(386, sparkSettings.getParallelism());
    assertEquals(39, sparkSettings.getExecutorNumbers());
    assertEquals("70G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void b12RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 1_277_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(694, sparkSettings.getParallelism());
    assertEquals(CONFIG.executorNumbersMax, sparkSettings.getExecutorNumbers());
    assertEquals(CONFIG.executorMemoryGbMax + "G", sparkSettings.getExecutorMemory());
  }

  @Test
  public void b15RecordsSettingsTest() {

    // State
    long fileRecordsNumber = 1_500_000_000;

    // When
    SparkSettings sparkSettings = SparkSettings.create(CONFIG, fileRecordsNumber, false);

    // Should
    assertNotNull(sparkSettings);
    assertEquals(CONFIG.parallelismMax, sparkSettings.getParallelism());
    assertEquals(CONFIG.executorNumbersMax, sparkSettings.getExecutorNumbers());
    assertEquals(CONFIG.executorMemoryGbMax + "G", sparkSettings.getExecutorMemory());
  }
}
