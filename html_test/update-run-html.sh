#!/usr/bin/env bash
set -euo pipefail

HTML_FILE="run.html"
TMP_FILE="$(mktemp)"

if [ ! -f "$HTML_FILE" ]; then
  echo "Fichier introuvable: $HTML_FILE" >&2
  exit 1
fi

if ! grep -q '<!-- TEST_SCRIPTS_START -->' "$HTML_FILE"; then
  echo "Marqueur manquant dans $HTML_FILE : <!-- TEST_SCRIPTS_START -->" >&2
  exit 1
fi

if ! grep -q '<!-- TEST_SCRIPTS_END -->' "$HTML_FILE"; then
  echo "Marqueur manquant dans $HTML_FILE : <!-- TEST_SCRIPTS_END -->" >&2
  exit 1
fi

mapfile -t TEST_FILES < <(
  find . -maxdepth 1 -type f -name '*test*.js' \
    ! -name 'runner.js' \
    -printf '%f\n' | sort
)

{
  awk '
    /<!-- TEST_SCRIPTS_START -->/ {
      print
      print "__TEST_SCRIPTS_PLACEHOLDER__"
      skip=1
      next
    }
    /<!-- TEST_SCRIPTS_END -->/ {
      skip=0
      print
      next
    }
    !skip { print }
  ' "$HTML_FILE"
} > "$TMP_FILE"

SCRIPT_TAGS=""
for f in "${TEST_FILES[@]}"; do
  SCRIPT_TAGS+="<script src=\"$f\"></script>\n"
done

perl -0pe "s|__TEST_SCRIPTS_PLACEHOLDER__|$SCRIPT_TAGS|g" "$TMP_FILE" > "$HTML_FILE"
rm -f "$TMP_FILE"

echo "run.html mis à jour avec ${#TEST_FILES[@]} script(s) de test."