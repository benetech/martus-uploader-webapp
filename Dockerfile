FROM 814633283276.dkr.ecr.us-east-1.amazonaws.com/tomcat:8-jdk8

ENV JAVA_OPTS -server -Xmx512m -Xms256m -Djava.library.path=$CATALINA_HOME/lib:/usr/lib/x86_64-linux-gnu -agentpath:$CATALINA_HOME/lib/libjvmkill.so

COPY ./build/libs/file.war /usr/local/tomcat/webapps/ROOT.war
