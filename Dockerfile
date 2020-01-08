FROM openjdk:11-jdk
WORKDIR /usr/src/app
ADD . .
RUN ./mvnw package

FROM openjdk:11-jre-slim
COPY --from=0 /usr/src/app/target/workflow-relay-*-SNAPSHOT.jar /usr/bin/workflow-relay.jar
CMD ["java", "-ea", "-jar", "/usr/bin/workflow-relay.jar"]
EXPOSE 8080/tcp