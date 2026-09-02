#!/bin/bash
set -euo pipefail
BASE=/home/spligan/.gradle/caches/ng_execute/1e75bb06b156e30858ebb6e86d61c54216f166856f2b6cbef573e7344602483a/transformed
sed -n '1,130p' "$BASE/net/minecraft/core/component/DataComponentInitializers.java"
echo '==== ReloadableServerResources ===='
grep -n -A5 'DATA_COMPONENT_INITIALIZERS' "$BASE/net/minecraft/server/ReloadableServerResources.java"
