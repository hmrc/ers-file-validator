#!/usr/bin/env bash

sbt clean scalastyleAll compile coverage test it/test coverageOff coverageReport dependencyUpdates
