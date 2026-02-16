# Building and Accessing ATG API Documentation

The ATG API javadocs are automatically downloaded by Maven when you build the project (`io.github.brandeis-cosi-103a:atg-api`). ATG API Documentation

The ATG API javadocs are automatically downloaded by Maven when you build the project (`io.github.brandeis-cosi-103a:atg-api`).

## How It Works

The `automation/pom.xml` includes a Maven profile (`local-dev-docs`) that automatically downloads sources and javadocs for the ATG API during the `initialize` phase. 

**Profile Activation:**
- ✅ **Activated in local development** (when `CI` environment variable is not set)
- ❌ **Skipped in CI/GitHub Actions** (GitHub Actions sets `CI=true`)

This happens automatically during local builds:
- `cd automation && mvn compile`
- `cd automation && mvn test`
- `cd automation && mvn package`
- Or explicitly: `cd automation && mvn initialize`

Alternatively, from the repo root: `mvn --file automation/pom.xml <phase>`

Downloaded javadocs are stored in your local Maven repository: `~/.m2/repository/io/github/brandeis-cosi-103a/atg-api/{version}/atg-api-{version}-javadoc.jar`

To verify the download worked, check that the javadoc JAR exists:
```bash
ls -lh ~/.m2/repository/io/github/brandeis-cosi-103a/atg-api/1.5.2/atg-api-1.5.2-javadoc.jar
```

## Benefits

### For AI Assistants (GitHub Copilot)
The IDE's Java language server indexes downloaded sources and javadocs from your Maven repository. Through LSP (Language Server Protocol) integration, this enhances Copilot's ability to:
- Suggest accurate method signatures and parameters
- Understand return types and exceptions
- Autocomplete with correct ATG API classes
- Provide contextually relevant code suggestions

**Note**: The manual API reference in `.github/copilot-instructions.md` provides additional direct context to Copilot since it's in the workspace.

### For Developers
- **IDE Integration**: Hover tooltips, go-to-definition, and inline documentation work seamlessly
- **Auto-Updates**: When you update the ATG API version in `pom.xml`, Maven automatically downloads the new javadocs
- **No Manual Maintenance**: No need to commit large HTML files or manually update documentation
- **Live Website**: For browsing HTML docs, visit: https://brandeis-cosi-103a.github.io/atg-api/

## Updating the API Version

Simply change the version in `automation/pom.xml`:
```xml
<dependency>
  <groupId>io.github.brandeis-cosi-103a</groupId>
  <artifactId>atg-api</artifactId>
  <version>1.5.2</version> <!-- Change this -->
</dependency>
```

Then run `mvn initialize` (or any build command) to download the new version's javadocs.
