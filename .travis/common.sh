#!/bin/bash

TC_VERSION=$(cd TotalCrossSDK && ./gradlew properties -q | grep 'version: ' | awk '{print $2}' | tr -d '[:space:]')
