#!/bin/bash
set -euo pipefail
BASE=/home/spligan/.gradle/caches/ng_execute/1e75bb06b156e30858ebb6e86d61c54216f166856f2b6cbef573e7344602483a/transformed
sed -n '240,320p' "$BASE/net/minecraft/core/Holder.java"
echo '==== bind/setComponents ===='
grep -rn 'bindComponents\|setComponents\|DATA_COMPONENT_INITIALIZERS\|components = ' "$BASE/net/minecraft" --include='*.java' | head -50
echo '==== NeoForge GameData ===='
find /home/spligan/.gradle/caches -name 'GameData.java' 2>/dev/null | head -5
find /home/spligan/.gradle/caches -name '*RegistryManager*' 2>/dev/null | head -5
grep -rn 'DATA_COMPONENT_INITIALIZERS\|bindComponents\|applyComponent' /home/spligan/.gradle/caches/modules-2/files-2.1/net.neoforged --include='*.java' 2>/dev/null | head -30 || true
# also search in jar sources
find /home/spligan/.gradle/caches -name 'neoforge*sources*.jar' 2>/dev/null | head -5
