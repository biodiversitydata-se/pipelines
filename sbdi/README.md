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
1. Download shape files from [here](https://pipelines-shp.s3-ap-southeast-2.amazonaws.com/pipelines-shapefiles.zip) and extract into the `/data/pipelines-shp` directory
1. Create the following directory `/data/pipelines-data`
1. Run:
    ```
    mkdir /data/pipelines-vocabularies
    wget -O /data/pipelines-vocabularies/DegreeOfEstablishment.json "https://api.gbif.org/v1/vocabularies/DegreeOfEstablishment/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/EstablishmentMeans.json "https://api.gbif.org/v1/vocabularies/EstablishmentMeans/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/LifeStage.json "https://api.gbif.org/v1/vocabularies/LifeStage/releases/LATEST/export"
    wget -O /data/pipelines-vocabularies/Pathway.json "https://api.gbif.org/v1/vocabularies/Pathway/releases/LATEST/export"
    ```
1.  Run:
    ```
    mkdir -p /data/pipelines-all-datasets/index-record/dr893
    touch /data/pipelines-all-datasets/index-record/dr893/dummy.avro
    ```
1. `cd livingatlas`
1. `git checkout dev`
1. Build with maven `mvn clean package -P skip-coverage,livingatlas-artifacts -T 1C -DskipTests -nsu`
1. Setup solr:
   1. Clone [biocache-solr](https://github.com/biodiversitydata-se/biocache-solr) and [biocache-service](https://github.com/biodiversitydata-se/biocache-service)
   2. In biocache-service, run `docker-compose up -d solr` so start solr
   3. In biocache-solr, run `make update-solr-config` to create or recreate the Solr collection

### Running la-pipelines
1. Start required docker containers using
    ```bash
    docker-compose -f pipelines/src/main/docker/ala-sensitive-data-service.yml up -d
    ```
1. `cd scripts`
1. To download from Collectory, run `./la-pipelines copy dr15`. File is saved to `/data/dwca-export`
1. To copy to location used by next step, run `cp -r /data/dwca-export/dr15 /data/biocache-load/`
1. To convert DwCA to AVRO, run `./la-pipelines dwca-avro dr15`
1. To interpret, run `./la-pipelines interpret dr15 --embedded`
1. To mint UUIDs, run `./la-pipelines uuid dr15 --embedded`
1. To check for sensitive data, run `./la-pipelines sds dr15 --embedded`
1. Optionally, if dataset has images  (don't run against production image service):
   1. Push images to image service, run `./la-pipelines image-load dr15 --embedded`
   1. Sync from image service, run `./la-pipelines image-sync dr15 --embedded`
1. To create index avro files, run `./la-pipelines index dr15 --embedded`
1. To sample, run `./la-pipelines sample dr15 --embedded`
1. To generate the SOLR index, run `./la-pipelines solr dr15 --embedded`
