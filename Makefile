build:
	mvn clean package -P skip-coverage,livingatlas-artifacts -T 1C -DskipTests -nsu
