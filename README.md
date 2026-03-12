# group-e
## Formatting
- To install the pre-push hook run:
``` bash
git config core.hooksPath .githooks
```
- To check formatting run `mvn spotless:check` from `automation`
- To fix formatting run `mvn spotless:apply` from `automation`
- To have spotless ignore a segment of code put `//spotless:off` before it and `//spotless:on` after  it

## Building/Testing
- The following command is used to build on github actions. Use it to test whether it will build. It must be run from the root. `build.sh` just contains the same command but as a bash script for convenience.
``` bash
mvn -B package --file automation/pom.xml
```
- You can use `mvn test` from `automation` to only run tests


---

Ana Obradovic - anaobradovic@brandeis.edu

Nathaniel Martin - nathanielmartin@brandeis.edu
- This game of tug of war has too many pull requests.

Lior Schifrin - liorschifrin@brandeis.edu

Oneg Kaufman Grob - onegkg@gmail.com
