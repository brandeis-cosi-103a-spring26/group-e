#!/usr/bin/env bash
set -euo pipefail
git rev-parse --git-dir > /dev/null 2>&1 || { echo "Not a git repo"; exit 1; }


STASHED=false
trap '[[ "$STASHED" == "true" ]] && git stash pop --quiet --index' EXIT


if [[ -n "$(git status --porcelain)" ]]; then
git stash push --quiet -u
STASHED=true
fi


mvn --file automation/pom.xml spotless:apply


git add -u
if ! git diff --cached --quiet; then
git commit -m "${1:-formatting}"
else
echo "No formatting changes"
fi