#!/usr/bin/env bash
set -euo pipefail
status=0
gradle "$@" || status=$?
rm -f "$0"
exit "$status"
