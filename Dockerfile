###############################
# Maven builder
###############################
# -alpine-slim image does not support --release flag
FROM adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine-slim as builder
WORKDIR /usr/src/app
COPY . .
RUN ./mvnw clean package -DskipTests

##############################
# Server
##############################
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine as server

# Paths
ENV APP_HOME /usr/src/app
ENV JAR_FILE $APP_HOME/workflow-relay.jar
ENV APP_USER wfuser
ENV APP_UID 9999
ENV APP_GID 9999

RUN addgroup -S -g $APP_GID $APP_USER  \
    && adduser -S -u $APP_UID -G $APP_USER $APP_USER  \
    && mkdir -p $APP_HOME \
    && chown -R $APP_UID:$APP_GID $APP_HOME

COPY --from=builder /usr/src/app/target/workflow-relay-*.jar $JAR_FILE

USER $APP_UID

WORKDIR $APP_HOME

CMD java -ea -jar $JAR_FILE
EXPOSE 8080/tcp
