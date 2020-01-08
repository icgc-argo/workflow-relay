FROM openjdk:11-jdk as builder
WORKDIR /usr/src/app
ADD . .
RUN ./mvnw clean package

FROM openjdk:11-jre-slim
COPY --from=builder /usr/src/app/target/workflow-relay-*.jar /usr/bin/workflow-relay.jar
CMD ["java", "-ea", "-jar", "/usr/bin/workflow-relay.jar"]
EXPOSE 8080/tcp
