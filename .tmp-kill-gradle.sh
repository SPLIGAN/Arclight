#!/bin/bash
pkill -f 'gradlew build collect' || true
pkill -f 'GradleDaemon' || true
sleep 2
pgrep -af '[g]radle' || echo no-gradle
