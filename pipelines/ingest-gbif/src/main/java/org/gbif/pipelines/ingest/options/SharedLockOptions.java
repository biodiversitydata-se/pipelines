package org.gbif.pipelines.ingest.options;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

/**
 * Pipeline Options to create Zookeeper shared locks using a ExponentialBackoffRetry strategy to connect.
 */
public interface SharedLockOptions extends PipelineOptions {

  @Description("List of Zookeeper servers to connect to")
  String getZkConnectionString();
  void setZkConnectionString(String zkConnectionString);

  @Description("Zookeeper shared path or space")
  String getNamespace();
  void setNamespace(String namespace);

  @Description("Base locking path path to use for locking")
  String getLockingPath();
  void setLockingPath(String lockingPath);


  @Description("Shared-lock name")
  String getLockName();
  void setLockName(String lockName);


  @Description("Initial amount of time to wait between retries")
  @Default.Integer(100)
  int getSleepTimeMs();
  void setSleepTimeMs(int sleepTimeMs);


  @Description("Max number of times to retry")
  @Default.Integer(5)
  int getMaxRetries();
  void setMaxRetries(int maxRetries);

}
