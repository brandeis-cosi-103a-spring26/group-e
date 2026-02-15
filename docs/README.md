# ATG API Documentation

The ATG API javadocs are automatically downloaded by Maven when you build the project (`io.github.brandeis-cosi-103a:atg-api`).

## How It Works

The `automation/pom.xml` includes the `maven-dependency-plugin` configured to automatically download sources and javadocs for all dependencies during the `initialize` phase. This happens automatically during:
- `mvn compile`
- `mvn test`
- `mvn package`
- Or explicitly: `mvn initialize`

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
