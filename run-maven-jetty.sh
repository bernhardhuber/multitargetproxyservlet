#!/bin/sh 

export JAVA_HOME=/home/sdkman/.sdkman/candidates/java/21.0.2-librca

echo "arg: $1"

if [ -z "$1" ]; then
  JETTY_CMD="jetty:run-war"
else 
  JETTY_CMD="$1"
fi

echo "Running jetty command $JETTY_CMD"

mvn -Pjetty $JETTY_CMD
