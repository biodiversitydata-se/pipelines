package au.org.ala.pipelines.options;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

/** Main pipeline options necessary for SOLR index for Living atlases */
public interface SolrPipelineOptions extends IndexingPipelineOptions {

  @Description("SOLR collection to index into")
  @Default.String("biocache")
  String getSolrCollection();

  void setSolrCollection(String solrCollection);

  @Description("List of Zookeeper hosts.")
  String getZkHost();

  void setZkHost(String zkHosts);

  @Description("SOLR batch size")
  @Default.Integer(500)
  Integer getSolrBatchSize();

  void setSolrBatchSize(Integer solrBatchSize);

  @Description("SOLR max retry attempts")
  @Default.Integer(10)
  Integer getSolrRetryMaxAttempts();

  void setSolrRetryMaxAttempts(Integer solrRetryMaxAttempts);

  @Description("SOLR max retry attempts")
  @Default.Integer(3)
  Integer getSolrRetryDurationInMins();

  void setSolrRetryDurationInMins(Integer solrRetryDurationInMins);

  @Description("Include sampling")
  @Default.Boolean(false)
  Boolean getIncludeSampling();

  void setIncludeSampling(Boolean includeSampling);

  @Description("JackKnife path")
  @Default.String("")
  String getJackKnifePath();

  void setJackKnifePath(String jackKnifePath);

  @Description("Include jackknife")
  @Default.Boolean(false)
  Boolean getIncludeJackKnife();

  void setIncludeJackKnife(Boolean includeJackKnife);

  @Description("Include clustering")
  @Default.Boolean(false)
  Boolean getIncludeClustering();

  void setIncludeClustering(Boolean includeClustering);

  @Description("Path to clustering avro files")
  @Default.String("")
  String getClusteringPath();

  void setClusteringPath(String clusteringPath);

  @Description("Number of partitions to use")
  @Default.Integer(1)
  Integer getNumOfPartitions();

  void setNumOfPartitions(Integer numOfPartitions);
}