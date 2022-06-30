package org.gbif.pipelines.diagnostics;

import org.gbif.pipelines.diagnostics.common.HbaseServer;
import org.junit.Test;

public class MainToolIT {

  @Test(expected = IllegalArgumentException.class)
  public void noToolKeyTest() {

    // State
    String[] args = {
      "--dataset-key",
      "508089ca-ddb4-4112-b2cb-cb1bff8f39ad",
      "--lookup-table",
      HbaseServer.CFG.getLookupTable(),
      "--deletion-strategy",
      "BOTH",
      "--only-collisions"
    };

    // When
    MainTool.main(args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void toolKeyTest() {
    // State
    String[] args = {
      "--file-path",
      "/test",
      "--from-dataset",
      "from",
      "--to-dataset",
      "to",
      "--tool",
      "MIGRATOR",
      "--lookup-table",
      HbaseServer.CFG.getLookupTable()
    };

    // When
    MainTool.main(args);
  }
}