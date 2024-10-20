#!/usr/bin/env bash

mvn versions:set -DnewVersion=2.0-SNAPSHOT
mvn -N versions:update-child-modules
mvn clean package install