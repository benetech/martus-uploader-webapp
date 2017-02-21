FROM 814633283276.dkr.ecr.us-east-1.amazonaws.com/tomcat:8-jdk8

ENV MIN_HEAP 256m
ENV MAX_HEAP 512m
ENV MAX_METASPACE 256m

COPY ./build/libs/file.war /usr/local/tomcat/webapps/ROOT.war
