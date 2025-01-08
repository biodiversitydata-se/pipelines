## Set up local environment
This is an adapted version of [Getting started in livingatlas/README.md](../livingatlas/README.md#Getting+started). 

### Software requirements:
* Java 8 - this is mandatory (see [GBIF pipelines documentation](https://github.com/gbif/pipelines#about-the-project)) (`sdk use java 8.0.402-tem`)
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
2. Create directory:
    ```
    mkdir /data/pipelines-clustering
    mkdir /data/pipelines-jackknife
    ```
3. Download vocabularies:
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

To run steps 2-10 in one go use the [sbdi-load](../livingatlas/scripts/sbdi-load) script.

Disabling taxon matching on taxonId is necessary for iNaturalist.
```
./la-pipelines interpret dr986 --embedded --config=../configs/la-pipelines.yaml,../configs/la-pipelines-local.yaml,../configs/disable-match-on-taxonid.yaml
```
Clustering is run on all indexed datasets (`/data/pipelines-all-datasets/index-record`) before the `solr` step.
```
./la-pipelines clustering all --embedded
```
#### Experimental
Jackknife is run on all indexed datasets (`/data/pipelines-all-datasets/index-record`) before the `solr` step. It also requires sampling for all datasets.
```
./la-pipelines sample all --embedded
./la-pipelines jackknife all --embedded
```

## Production
There are currently one manager node (live-pipelines-1) and six worker nodes (live-pipelines-2 - live-pipelines-7) for running pipelines. 

The solr cloud consist of three nodes (live-solrcloud-1, -2 and -3).

See *pipelines* and *solrcloud* roles in [sbdi-install](https://github.com/biodiversitydata-se/sbdi-install) for information on how to install and manage pipelines.

### Running pipelines
Run pipelines as the `spark` user. A recommendation is to use Linux [screen command](https://www.gnu.org/software/screen/manual/screen.html), especially when loading large datasets.  

Use `sbdi-load` (/usr/bin/sbdi-load) to run all the pipeline steps for a single dataset:
```
sbdi-load dr11
```

`sbdi-load` will look for dataset-specific configuration in `/data/la-pipelines/config/dr.config`.

`sbdi-load` runs the following pipeline steps:
- copy
- dwca-avro
- interpret
- uuid
- sds
- image-load (only if specified in `dr.config`)
- image-sync (only if specified in `dr.config`)
- index
- sample
- solr
- [delete removed records](#Deleting-removed-records-in-SOLR)
- [backup of identifiers](#backup)

Use `la-pipelines` (/usr/bin/la-pipelines) to run single pipeline steps:
```
la-pipelines interpret dr11 > /data/log/dr11/$(date +%y%m%d-%H%M%S).log 2>&1
```
There is also a script for loading datasets from a queue file: `load-queue`. The queue file is expected to be found at `/data/load-queue/queue.txt` and contain the datasets to be loaded on separate lines.

#### Clustering
Clustering is run on all indexed datasets before the solr step:
```
la-pipelines clustering all > /data/log/clustering/$(date +%y%m%d-%H%M%S).log 2>&1
```
In order to support various load scenarios for clustering `sbdi-load` and `load-queue` have a *mode* parameter that can be one of the following:
* `default` (or unspecified) - run all pipelines steps as listed above
* `skip_solr` - run all pipelines steps except solr
* `only_solr` - run only the solr step
* `clustering` - run all pipelines steps as listed above plus clustering (before solr)

Examples:
```
sbdi-load dr11 skip_solr
load-queue skip_solr
```

### Logs
When you run `sbdi-load` a log file will be created in `/data/log/dr[X]`.

Each spark worker node (live-pipelines-2 - live-pipelines-7) also creates a log for every spark application it runs. The logs can be found in `/data/spark/work`. This directory also contains a copy of the jar-file used. The size of this directory can grow quickly when many datasets are loaded. There is an ansible task in [sbdi-install](https://github.com/biodiversitydata-se/sbdi-install) to clear it out on all nodes.  

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

### Deploy new pipelines jar-file
The pipelines jar file is built on github and can be accessed as a workflow artifact. Follow these steps to deploy a newly built jar file.
* Download artifact from the last workflow run: https://github.com/biodiversitydata-se/pipelines/actions
* Unzip:
  ```
  unzip package.zip -d . 
  ``` 
* Upload to S3:
  ```
  s3cmd put -P pipelines-2.18.7-SNAPSHOT-shaded.jar s3://install/la-pipelines.jar 
  ``` 
* Deploy:
  ```
  ansible-playbook -i inventories/prod pipelines.yml -t pipelines-jar --ask-become-pass
  ``` 

### Deleting removed records in SOLR
Pipelines don't handle removal of records from SOLR. If records have been removed from the source dataset they need to be removed manually from SOLR.

Datasets having too many records in the Atlas can be found in the [IPT vs Atlas view](https://collections.biodiversitydata.se/ipt/syncView?uid=dp0&sort=title&order=asc&onlyUnsynced=true) in the Collectory.

The specific records can be found with the following SOLR query (using the AND operator). Don't forget to adjust the TO date. The TO should be something like the day before the current date.
```
dataResourceUid:dr964
lastLoadDate:[2024-01-01T00:00:00Z TO 2024-04-01T00:00:00Z]
```

They can be deleted using the following script:
```
delete-deleted-records dr964 2024-04-01
```

~~They can be [deleted](https://medium.com/@mgasanthosh/solr-deleting-the-document-3c6a6046a1f6) with the same query:~~
```
<delete>
 <query>
  (dataResourceUid:dr964) AND (lastLoadDate:[2024-01-01T00:00:00Z TO 2024-04-01T00:00:00Z])
 </query>
</delete>
```

### Other things to watch
* If the unique term of a dataset is changed the uuid step will most likely fail because too many records have changed id:s. This validatiation can be overriden like this:
  ```
  la-pipelines uuid dr4 --extra-args=overridePercentageCheck=true > /data/log/dr4/$(date +%y%m%d-%H%M%S).log 2>&1
  ```
* Spark accumulates data in `/data/spark/work` on the worker nodes. This has been addressed by setting `spark.worker.cleanup.enabled=true`.
* The docker services accumulate data in `/var/lib/docker/containers` which may fill up the root volume. This data is cleared when the docker services are stopped and removed (they start again on reboot).
* If Artportalen fails with random errors, try restarting all machines. This is to clear up memory used by the docker services (and possibly other stuff).

### Useful commands

#### Spark
```
sudo su - spark
spark-cluster.sh --stop
spark-cluster.sh --start
```

#### Hadoop
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

#### Solr

Remove all entries for a dataset:
* https://medium.com/@mgasanthosh/solr-deleting-the-document-3c6a6046a1f6
* https://stackoverflow.com/questions/23228727/deleting-solr-documents-from-solr-admin/48007194#48007194

```
<delete><query>dataResourceUid:dr18</query></delete>
```

### Backup
The unique identifiers for each dataset are stored on hadoop in `/pipelines-data/dr[X]/1/identifiers`. There is also a backup on `live-pipelines-1:/data/backup/pipelines-data`. Identifiers are copied to the backup directory as the last step in `sbdi-load`.

To backup the identifiers manually for a dataset run this command (as the `spark` user):
```
backup-dr dr0
```
For all datasets:
```
backup-all > /data/backup/$(date +%y%m%d-%H%M%S).log 2>&1
```

The identifiers and the log directory are also copied (manually by using the `backup-pipelines.sh` script) to `nrm-sbdibackup`.
