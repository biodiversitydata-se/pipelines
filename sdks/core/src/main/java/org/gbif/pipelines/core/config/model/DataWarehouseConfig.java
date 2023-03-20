package org.gbif.pipelines.core.config.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWarehouseConfig implements Serializable {

  /** JDBC Connection String to Hive or the underlying data warehouse */
  private String connectionString;

  /** Data warehouse username */
  private String username;

  /** Data warehouse password */
  private String password;

  /** Directory from where the Data warehouse reads the data */
  private String externalStorePath;
}
