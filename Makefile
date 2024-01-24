build:
	# gbif/ingestion/pre-backbone-release fails because of referring to an invalid domain (http://conjars.org).
	# Everything seems to work fine without it so it is skipped.
	mvn clean install -P skip-coverage,livingatlas-artifacts -pl '!gbif/ingestion/pre-backbone-release' -DskipTests -Dspotless.check.skip -nsu -T 1C
