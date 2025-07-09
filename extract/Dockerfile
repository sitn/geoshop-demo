# Extract builder
FROM ubuntu:oracular AS builder
LABEL Maintainer="andrey.rusakov@camptocamp.com" Vendor="Camptocamp"

WORKDIR /extract-build

ENV EXTRACT_VERSION="${EXTRACT_VERSION:-master}" \
    GEOSHOP_CONNECTOR_VERSION="${GEOSHOP_CONNECTOR_VERSION:-main}"

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git openjdk-21-jdk maven npm && \
    npm install -g corepack && \
    git clone https://github.com/asit-asso/extract && \
    git clone https://github.com/sitn/sitn_geoshop_connector

COPY extract-task-exec /extract-build/extract-task-exec

# Copy custom application.properties file to the build folder
COPY config/application.properties /extract-build/application.properties

RUN cd /extract-build/sitn_geoshop_connector/ && git checkout "${GEOSHOP_CONNECTOR_VERSION}" && \
    cd /extract-build/sitn_geoshop_connector/plugin-interface && \
    mvn clean install && \
    cd /extract-build/sitn_geoshop_connector/connectors/extract-connector-geoshop/ && \
    mvn clean install && \
    cp target/extract-connector-geoshop-*.jar /extract-build/extract/extract/src/main/resources/connectors && \
    cd /extract-build/extract/ && git checkout "${EXTRACT_VERSION}" && \
    cp /extract-build/application.properties /extract-build/extract/extract/src/main/resources/application.properties && \
    cd /extract-build/extract/extract-interface && \
    mvn clean install && \
    cd /extract-build/extract-task-exec && \
    mvn clean install  && \
    cp /extract-build/extract-task-exec/target/original-extract-task-exec-*.jar /extract-build/extract/extract/src/main/resources/task_processors/extract-task-exec.jar

RUN cd /extract-build/extract/ && git checkout "${EXTRACT_VERSION}" && \
    cd /extract-build/extract/extract/ && \
    bash install_dependencies.sh && \
    cd /extract-build/extract && \
    mvn clean install && \
    # Folder has only one war file, the final build result
    cp -v /extract-build/extract/extract/target/*war /extract-build/extract-${EXTRACT_VERSION}.war

# Extract service
FROM tomcat:9-jre17
LABEL Maintainer="andrey.rusakov@camptocamp.com" Vendor="Camptocamp"

ENV GEODATA_POSTGRES_HOST="${GEODATA_POSTGRES_HOST:-pgsql}" \
    GEODATA_POSTGRES_PORT="${GEODATA_POSTGRES_PORT:-5432}" \
    GEODATA_POSTGRES_USER="${GEODATA_POSTGRES_USER:-extractuser}" \
    GEODATA_POSTGRES_PASSWORD="${GEODATA_POSTGRES_PASSWORD:-demopassword}" \
    GEODATA_POSTGRES_DB="${GEODATA_POSTGRES_DB:-extract}" \
    EXTERNAL_URL="${EXTERNAL_URL:-http://localhost:8080}" \
    ENCRYPTION_SECRET="${ENCRYPTION_SECRET:-CHANGEMEchangemeCHANGEMEchangeme}" \
    ENCRYPTION_SALT="${ENCRYPTION_SALT:-CHANGEMEchangemeCHANGEMEchangeme}"

RUN apt update && apt upgrade -y && \
    apt install -y curl python3 python3-geopandas python3-psycopg2 python3-gdal && \
    apt install -y software-properties-common && \
    mkdir -p /var/extract/orders && mkdir -p /extract/

COPY --from=builder /extract-build/extract-*.war /usr/local/tomcat/webapps/extract.war
COPY tomcat/conf/context.xml /usr/local/tomcat/conf/context.xml
COPY script.py /extract/script.py

RUN mkdir -p /var/log/extract
RUN chown -R www-data:www-data /usr/local/tomcat/ /var/log/extract/ /var/extract/orders /extract/

USER www-data
