#!/bin/bash

DR=$1

./la-pipelines dwca-avro $DR
./la-pipelines interpret $DR --embedded
./la-pipelines uuid $DR --embedded
./la-pipelines sds $DR --embedded
#./la-pipelines image-load $DR --embedded
#./la-pipelines image-sync $DR --embedded
./la-pipelines index $DR --embedded
./la-pipelines sample $DR --embedded
./la-pipelines solr $DR --embedded
