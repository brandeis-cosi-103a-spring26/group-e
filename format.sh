#!/usr/bin/env bash

STASHED=false
if ! git diff --quiet HEAD; then
    git stash push --quiet -u
    STASHED=true
fi

mvn --file automation/pom.xml spotless:apply

git add -A

if ! git diff --cached --quiet; then
    git commit -m "formatting"
else
    echo "No formatting changes made, nothing to commit"
fi

if [[ "$STASHED" == "true" ]]; then
    git stash pop --quiet --keep-index
fi
