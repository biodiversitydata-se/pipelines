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
1. Build:
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
1. Start required docker containers using
    ```bash
    docker-compose -f pipelines/src/main/docker/ala-sensitive-data-service.yml up -d
    ```
1. `cd livingatlas/scripts`
1. To download from Collectory, run `./la-pipelines copy dr15`. File is saved to `/data/biocache-load`
1. To convert DwCA to AVRO, run `./la-pipelines dwca-avro dr15`
1. To interpret, run `./la-pipelines interpret dr15 --embedded`
1. To mint UUIDs, run `./la-pipelines uuid dr15 --embedded`
1. To check for sensitive data, run `./la-pipelines sds dr15 --embedded`
1. Optionally, if dataset has images  (don't run against production image service):
   1. Push images to image service, run `./la-pipelines image-load dr15 --embedded`
   1. Sync from image service, run `./la-pipelines image-sync dr15 --embedded`
1. To create index avro files, run `./la-pipelines index dr15 --embedded` (To index images, run: `./la-pipelines index dr15 --embedded --extra-args=includeImages=true` 
1. To sample, run `./la-pipelines sample dr15 --embedded` (If the dataset lacks sampling info, run: `./la-pipelines sample dr15 --embedded --extra-args=includeSampling=false`)
1. To generate the SOLR index, run `./la-pipelines solr dr15 --embedded`

To run steps 4-11 in one go use the [sbdi-load](../livingatlas/scripts/sbdi-load) script.
