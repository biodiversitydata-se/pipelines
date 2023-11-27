#!/bin/bash

# Exit on errors
set -e

dr=$1
dr_data=/data/pipelines-data/$dr/1

./la-pipelines dwca-avro $dr
./la-pipelines interpret $dr --embedded
./la-pipelines uuid $dr --embedded
./la-pipelines sds $dr --embedded
if grep -q multimediaRecordsCountAttempted "$dr_data/interpretation-metrics.yml"; then
  ./la-pipelines image-load $dr --embedded
  ./la-pipelines image-sync $dr --embedded
fi
./la-pipelines index $dr --embedded
./la-pipelines sample $dr --embedded
if [ ! -d "$dr_data/sampling" ]; then
  extra_args="--extra-args=includeSampling=false"
fi
./la-pipelines solr $dr --embedded $extra_args
