# ATG API Documentation

The ATG API javadocs are automatically downloaded by Maven when you build the project (`io.github.brandeis-cosi-103a:atg-api`).

## How It Works

The `automation/pom.xml` includes the `maven-dependency-plugin` configured to automatically download sources and javadocs for all dependencies during the `initialize` phase. This happens automatically during:
- `mvn compile`
- `mvn test`
- `mvn package`
- Or explicitly: `mvn initialize`

Downloaded javadocs are stored in your local Maven repository: `~/.m2/repository/io/github/brandeis-cosi-103a/atg-api/{version}/atg-api-{version}-javadoc.jar`

## Benefits

### For AI Assistants (GitHub Copilot)
The IDE's language server indexes downloaded sources and javadocs, making them available through LSP integration. Copilot can:
- Know exact method signatures and parameters
- Understand return types and exceptions
- Discover utility methods and classes
- Provide accurate code completions
- Suggest idiomatic usage patterns

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
