#!/usr/bin/env bash
set -euo pipefail
name=$(python3 -c "import json;print(json.load(open('.specify/feature.json'))['name'])" 2>/dev/null || true)
echo "specs/${name}"
