#!/usr/bin/env bash

sbt clean scalafmtAll compile coverage test it/test coverageOff coverageReport dependencyUpdates
