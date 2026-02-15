# ATG API Documentation

This directory contains the complete Javadoc documentation for the ATG API (`io.github.brandeis-cosi-103a:atg-api:1.5.2`).

## Purpose

These javadocs are essential for GitHub Copilot and other AI coding assistants to provide accurate suggestions and understand the ATG API fully. With these docs in the repository, Copilot can:

- Know exact method signatures and parameters
- Understand return types and exceptions
- Discover utility methods and classes
- Provide accurate code completions
- Suggest idiomatic usage patterns

## How to Use

### For AI Assistants
The javadocs are automatically available in your workspace context. When you ask Copilot about ATG API classes, methods, or patterns, it can reference these docs.

### For Developers
Browse the documentation starting at:
- **Main index**: `atg-api-javadoc/index.html`
- **All classes**: `atg-api-javadoc/allclasses-index.html`

Or open specific package documentation:
- `atg-api-javadoc/edu/brandeis/cosi/atg/engine/` - Game engine
- `atg-api-javadoc/edu/brandeis/cosi/atg/state/` - Game state
- `atg-api-javadoc/edu/brandeis/cosi/atg/cards/` - Card definitions
- `atg-api-javadoc/edu/brandeis/cosi/atg/decisions/` - Player decisions
- `atg-api-javadoc/edu/brandeis/cosi/atg/player/` - Player interface

## Regenerating Documentation

If the ATG API version is updated, regenerate the javadocs:

```bash
cd automation
mvn dependency:copy -Dartifact=io.github.brandeis-cosi-103a:atg-api:NEW_VERSION:jar:javadoc -DoutputDirectory=../docs
cd ../docs
rm -rf atg-api-javadoc
unzip -q atg-api-NEW_VERSION-javadoc.jar -d atg-api-javadoc
```

Replace `NEW_VERSION` with the updated version number from `pom.xml`.
