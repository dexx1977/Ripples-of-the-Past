#!/bin/sh
# Query the mapped Forge 1.20.1 development artifact: the authoritative API
# surface for this port (official/Mojang names, Forge patches applied).
#   api.sh find <regex>          list matching classes
#   api.sh sig <binary name>     show members of one class
#   api.sh grep <regex>          show member signatures matching a regex
set -e
cd "$(dirname "$0")/../.."
JAR=$(ls build/fg_cache/net/minecraftforge/forge/*_mapped_official_*/forge-*_mapped_official_*.jar | head -1)
[ -f "$JAR" ] || { echo "mapped jar not built yet" >&2; exit 1; }
JDK=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")
case "$1" in
  find) unzip -l "$JAR" | awk '{print $NF}' | grep -E '\.class$' | grep -v '\$' | sed 's/\.class$//;s#/#.#g' | grep -E "$2" ;;
  sig)  "$JDK/bin/javap" -cp "$JAR" "$2" ;;
  grep) unzip -l "$JAR" | awk '{print $NF}' | grep -E '\.class$' | grep -v '\$' | sed 's/\.class$//;s#/#.#g' | while read -r c; do
          "$JDK/bin/javap" -cp "$JAR" "$c" 2>/dev/null | grep -E "$2" | sed "s#^#$c :: #"; done ;;
  *) echo "usage: api.sh find|sig|grep <arg>" >&2; exit 2 ;;
esac
