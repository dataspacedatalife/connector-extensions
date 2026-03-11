FROM eclipse-temurin:23.0.2_7-jre-alpine

ARG JVM_ARGS=""
ARG JAR

RUN apk --no-cache add curl bash

RUN curl -sSL https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh -o /wait-for-it.sh && chmod +x /wait-for-it.sh

WORKDIR /app

COPY ${JAR} identity-hub.jar

ENV ENV_JVM_ARGS=$JVM_ARGS
ENV ENV_APPINSIGHTS_AGENT_VERSION=$APPINSIGHTS_AGENT_VERSION
ENV USE_WAIT_FOR_IT="true"
ENV WEB_HTTP_PORT="8182"
ENV WEB_HTTP_PATH="/api/observability"

HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail http://localhost:8182/api/observability/check/health


CMD sh -c '\
  if [ "$USE_WAIT_FOR_IT" = "true" ]; then \
    /wait-for-it.sh $POSTGRES_HOST:$POSTGRES_PORT -- \
    /wait-for-it.sh $VAULT_HOST:$VAULT_PORT -- \
    java $ENV_JVM_ARGS -jar identity-hub.jar --log-level=debug; \
  else \
    java $ENV_JVM_ARGS -jar identity-hub.jar --log-level=debug; \
  fi'
