#!/bin/sh 

export JAVA_HOME=/home/sdkman/.sdkman/candidates/java/21.0.2-librca
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-armhf
#export JAVA_HOME=/home/sdkman/.sdkman/candidates/java/8.0.402-zulu

echo "arg: $1"

if [ -z "$1" ]; then
  WILDFLY_CMD="wildfly:run"
else 
  WILDFLY_CMD="$1"
fi

echo "Running wildfly command $WILDFLY_CMD"

mvn -Pwildfly $WILDFLY_CMD
