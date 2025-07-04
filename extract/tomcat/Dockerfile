FROM curlimages/curl:8.14.1 AS builder

WORKDIR /app
RUN curl -L -o extract.zip https://github.com/asit-asso/extract/releases/download/v2.2.0/extract_v2.2.0.zip \
  && unzip extract.zip

FROM tomcat:9.0.64-jre17

ENV TZ="Europe/Zurich"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy configuration
COPY --from=builder /app/application/* /usr/local/tomcat/webapps
COPY conf/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY conf/context.xml /usr/local/tomcat/conf/context.xml
COPY conf/manager-context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml

RUN mkdir -p /var/extract/orders
RUN mkdir -p /var/log/extract
EXPOSE 8080
