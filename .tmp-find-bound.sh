#!/bin/bash
set -euo pipefail
BASE=$(find /home/spligan/.gradle/caches/ng_execute -type d -name transformed 2>/dev/null | head -1)
echo "BASE=$BASE"
grep -rn 'Components not bound yet' "$BASE" --include='*.java' 2>/dev/null | head -30
echo '===='
STACK=$(find "$BASE" -name 'ItemStack.java' | head -1)
echo "STACK=$STACK"
grep -n 'bound\|public <.*> set\|setComponent\|PatchedDataComponent' "$STACK" | head -80
echo '==== Item ===='
ITEM=$(find "$BASE" -name 'Item.java' -path '*/world/item/Item.java' | head -1)
sed -n '140,200p' "$ITEM"
echo '==== ReferenceHolder components ===='
find "$BASE" -name '*.java' | xargs grep -l 'Components not bound yet' 2>/dev/null | head -10
