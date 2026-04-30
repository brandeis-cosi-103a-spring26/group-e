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

## Deploying to Azure Container Registry
To deploy the Docker image to the Azure Container Registry, follow these steps:

1. Log in to your Azure account:
   ```bash
   az login
   ```
2. Retrieve the admin credentials for the container registry (necessary if you lack RBAC push permissions):
   ```bash
   az acr credential show --name ServerATG
   ```
3. Log in to the Docker registry using the username (`ServerATG`) and one of the passwords from the previous command's output:
   ```bash
   docker login serveratg-akabbzauhefhc6ak.azurecr.io -u ServerATG -p <your-admin-password>
   ```
4. Push the image to the registry:
   ```bash
   docker push serveratg-akabbzauhefhc6ak.azurecr.io/group-e-server:v1
   ```

---

Ana Obradovic - anaobradovic@brandeis.edu

Nathaniel Martin - nathanielmartin@brandeis.edu
- This game of tug of war has too many pull requests.

Lior Schifrin - liorschifrin@brandeis.edu

Oneg Kaufman Grob - onegkg@gmail.com
