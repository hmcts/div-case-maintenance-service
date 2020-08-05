ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.4
ENV APP div-case-maintenance-service.jar

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

CMD ["div-case-maintenance-service.jar"]
