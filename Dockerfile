ARG APP_INSIGHTS_AGENT_VERSION=3.4.16
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

ENV APP div-case-maintenance-service.jar

COPY build/libs/$APP /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 4010

CMD ["div-case-maintenance-service.jar"]
