
STASHED=false
if ! git diff --quiet HEAD; then
    git stash push --quiet -u
    STASHED=true
fi

mvn --file automation/pom.xml spotless:apply

git add -A
git commit -m "formatting"

if ["$STASHED" = "true"]; then
    git stash pop --quiet
fi
