FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src
ARG WEB_ARCHIVE=https://github.com/Juansit-0/HayPaComer-web/archive/refs/heads/main.tar.gz
ADD ${WEB_ARCHIVE} /tmp/web.tar.gz
COPY . .
RUN mkdir -p /tmp/web brand/assets web/src/main/resources/static \
    && tar -xzf /tmp/web.tar.gz -C /tmp/web --strip-components=1 \
    && cp -R /tmp/web/assets/. brand/assets/ \
    && rm -rf /tmp/web/assets /tmp/web/README.md \
    && cp -R /tmp/web/. web/src/main/resources/static/ \
    && mvn -B -ntp -DskipTests -Djacoco.skip=true -Dspotless.check.skip=true -pl web -am package \
    && cp web/target/web-*.jar /src/app.jar

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S haypacomer && adduser -S haypacomer -G haypacomer
WORKDIR /app
COPY --from=build --chown=haypacomer:haypacomer /src/app.jar /app/app.jar
USER haypacomer
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- "http://127.0.0.1:${PORT}/actuator/health/readiness" > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
