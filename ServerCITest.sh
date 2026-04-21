#!/bin/sh

set -e

mvn -B package --file automation/pom.xml

mvn -f automatio/server/pom.xml spring-boot:run

curl -X POST localhost:8080/decide -H "Content-Type: application/json" -d @server-test-utils/request.json