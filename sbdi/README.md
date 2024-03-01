## Set up local environment
This is an adapted version of [Getting started in livingatlas/README.md](../livingatlas/README.md#Getting+started). 

### Software requirements:
* Java 8 - this is mandatory (see [GBIF pipelines documentation](https://github.com/gbif/pipelines#about-the-project)) (`sdk use java 8.0.392-tem`)
* Maven needs to run with OpenSDK 1.8
  'nano ~/.mavenrc' add 'export JAVA_HOME=[JDK1.8 PATH]'
* [Docker Desktop](https://www.docker.com/products/docker-desktop) or just plain Docker
* [lombok plugin for intelliJ](https://projectlombok.org/setup/intellij) needs to be installed for slf4 annotation
* Install `docopts` using the [prebuilt binary option](https://github.com/docopt/docopts#pre-built-binaries)
* Install `yq` via Brew (`brew install yq`)
* Optionally install the `avro-tools` package via Brew (`brew install avro-tools`)

### Setting up la-pipelines
1. Create symlink:
    ```
    ln -s /home/mats/src/biodiversitydata-se/pipelines/sbdi/pipelines-shp /data/
    ```
1. Create directory and symlink:
    ```
    mkdir -p /data/pipelines-data/resources
    ln -s /home/mats/src/biodiversitydata-se/pipelines/sbdi/stateProvinces.tsv /data/pipelines-data/resources/
    ```
2. Download vocabularies:
    ```
    mkdir /data/pipelines-vocabularies
    wget -O /data/pipelines-vocabularies/DegreeOfEstablishment.json "https://api.gbif.org/v1/vocabularies/DegreeOfEstablishment/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/EstablishmentMeans.json "https://api.gbif.org/v1/vocabularies/EstablishmentMeans/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/LifeStage.json "https://api.gbif.org/v1/vocabularies/LifeStage/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/Pathway.json "https://api.gbif.org/v1/vocabularies/Pathway/releases/LATEST/export"
    ```
1. Build jar-file:
   ```
   make build
   ```
1. Setup solr:
   1. Clone the [biocache-service](https://github.com/biodiversitydata-se/biocache-service) repo
   2. In biocache-service, run `docker-compose up -d solr` so start solr
   3. To create or recreate the Solr collection, run:
       ```
       cd livingatlas/solr/scripts
       ./update-solr-config.sh
       ```

### Running la-pipelines
1. `cd livingatlas/scripts`
1. To download from Collectory, run `./la-pipelines copy dr15`. File is saved to `/data/dwca-export`
1. To convert DwCA to AVRO, run `./la-pipelines dwca-avro dr15`
1. To interpret, run `./la-pipelines interpret dr15 --embedded`
1. To mint UUIDs, run `./la-pipelines uuid dr15 --embedded`
1. To check for sensitive data, run `./la-pipelines sds dr15 --embedded`
1. Optionally, if dataset has images  (don't run against production image service):
   1. Push images to image service, run `./la-pipelines image-load dr15 --embedded`
   1. Sync from image service, run `./la-pipelines image-sync dr15 --embedded`
1. To create index avro files, run `./la-pipelines index dr15 --embedded` (To index images, run: `./la-pipelines index dr15 --embedded --extra-args=includeImages=true` 
1. To sample, run `./la-pipelines sample dr15 --embedded`
1. To generate the SOLR index, run `./la-pipelines solr dr15 --embedded` (If the dataset lacks sampling info, run: `./la-pipelines solr dr15 --embedded --extra-args=includeSampling=false`)

To run steps 3-11 in one go use the [sbdi-load](../livingatlas/scripts/sbdi-load) script.

## Production
There are currently one manager node (live-pipelines-1) and six worker nodes (live-pipelines-2 - live-pipelines-7) for running pipelines. 

The solr cloud consist of three nodes (live-solrcloud-1, -2 and -3).

### Running pipelines
Run pipelines as the `spark` user.

Use `sbdi-load` (/usr/bin/sbdi-load) to run all the pipeline steps for a single dataset:
```
sbdi-load dr11
```
A log file will be created in `/data/log/dr11`

To also load images:
```
sbdi-load dr11 --load-images
```

`sbdi-load` runs the following pipeline steps:
- copy
- dwca-avro
- interpret
- uuid
- sds
- image-load (only if run with `--load-images`)
- image-sync (only if run with `--load-images`)
- index
- sample
- solr

Use `la-pipelines` (/usr/bin/la-pipelines) to run single pipeline steps:
```
la-pipelines interpret dr11 > /data/log/dr11/$(date +%y%m%d-%H%M%S).log 2>&1
```

### Monitoring

There are a number of web ui:s running on the various nodes that can be useful to monitor while running pipelines. SSL tunneling can be used to access them.

Solr cloud:
```
ssh -L 8973:127.0.0.1:8973 live-solrcloud-1
```

Hadoop manager:
```
ssh -L 50070:127.0.0.1:50070 live-pipelines-1
```

Hadoop workers:
```
ssh -L 50075:127.0.0.1:50075 live-pipelines-2
```

Spark manager:
```
ssh -L 8084:127.0.0.1:8080 live-pipelines-1
```

Spark workers:
```
ssh -L 8085:127.0.0.1:8085 live-pipelines-2
```

SparkContext (only available when Spark is executing tasks): 
```
ssh -L 4040:127.0.0.1:4040 live-pipelines-1
```

### Useful commands

Spark:
```
sudo su - spark
spark-cluster.sh --stop
spark-cluster.sh --start
```

Hadoop:
```
sudo su - hadoop
stop-dfs.sh
start-dfs.sh
```
```
hdfs dfsadmin -report
jps
```
```
hdfs dfs -ls /pipelines-data/
hdfs dfs -ls /pipelines-data/dr5/1
hdfs dfs -rm /pipelines-data/dr5/1/indexing-metrics.yml
hdfs dfs -rm -r  /pipelines-data/dr5

hdfs dfs -ls /pipelines-all-datasets/index-record
hdfs dfs -rm -r /pipelines-all-datasets/index-record/dr5

hdfs dfs -copyFromLocal -p /data/migration/target/dr91 /pipelines-data/
hdfs dfs -copyFromLocal -p * /pipelines-data/

hdfs dfs -copyToLocal /pipelines-data/dr37/1/latlng/latlng.csv /tmp
```

