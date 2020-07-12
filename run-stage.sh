#!/usr/bin/env bash

JAVA14_HOME=$SDKMAN_DIR/candidates/java/14.0.1.hs-adpt

NEW_SECRET="a long secret to appease the entropy gods"

cd target/universal/stage || exit

bin/memory-allocation-test -java-home "$JAVA14_HOME" -Dplay.http.secret.key="$NEW_SECRET"