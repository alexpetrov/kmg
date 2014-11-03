#!/bin/bash

export TIMBRE_LOG_LEVEL="trace"
export LOG_FILE_PATH="logs/kmg.log"
export DATABASE_URL="datomic:free://localhost:4334/kmg-sample"

java -jar target/kmg-0.1.0-SNAPSHOT-standalone.jar
