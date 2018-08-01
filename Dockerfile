FROM openjdk:8-jre-alpine
RUN apk add --update bash
RUN apk add --update curl && rm -rf /var/cache/apk/*

ARG JAR_FILE
COPY target/${JAR_FILE} /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8080
