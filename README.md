# workflow-relay
Microservice for injest of workflow events, routing to appropriate Kafka topics, and indexing into elasticsearch. 


## Build

With maven:
```bash
mvn clean package
```

With docker:
```bash 
docker build .
```

## Run
```bash
java -jar target/workflow-relay.jar
```