#!/usr/bin/env bash

sbt clean scalastyleAll compile coverage test IntegrationTest/test coverageOff coverageReport dependencyUpdates
