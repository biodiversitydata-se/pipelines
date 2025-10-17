build-jar:
	mvn install -P livingatlas-artifacts -DskipTests -Dspotless.check.skip -nsu -T 1C

build-ci:
	mvn --batch-mode package -P livingatlas-artifacts -DskipITs -nsu

test:
	mvn verify -P livingatlas-artifacts -DskipITs -nsu -T 1C
