#!/bin/bash

git submodule init
git submodule update
cd ppi-query
lein pom
cd -

mvn package -DskipTests=true

