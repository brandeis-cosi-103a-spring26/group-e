# Automation: The Game
An implementation of the deck-buliding card game Dominion in Java. Built by 4 developers for COSI 103A (Software Engineering) at Brandies University


![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
<br>
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Azure Container Registry](https://img.shields.io/badge/Azure_Container_Registry-0078D4?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)


### Built by:
- [Lior Schifrin](https://www.github.com/liorschifrin)
- [Ana Obradovic](https://github.com/anyaoooo-deis26)
- [Nathaniel Martin](https://github.com/nmartin47)
- [Oneg Kaufman Grob](https://github.com/onegkg)


Play with 2–4 human or automated players, build your deck with
cryptocurrency and software development themed cards, and compete to
earn the most victory points.

## Features

![ATG in action](/docs/images/ATG%20Screenshot.png)

- Interactive console gameplay
- 15 action cards, including attacks, defenses, and deck upgrades
- A randomized selection of 10 action cards per game
- Multiple rule-based automated player strategies
- Spring Boot API for remote player decisions and game events
- JUnit tests, Spotless formatting, and Docker packaging


<br>
<br>
<br>

## Developer Guide
### Formatting
- To install the pre-push hook run:
``` bash
git config core.hooksPath .githooks
```
- To check formatting run `mvn spotless:check` from `automation`
- To fix formatting run `mvn spotless:apply` from `automation`
- To have spotless ignore a segment of code put `//spotless:off` before it and `//spotless:on` after  it

### Building/Testing
- The following command is used to build on github actions. Use it to test whether it will build. It must be run from the root. `build.sh` just contains the same command but as a bash script for convenience.
``` bash
mvn -B package --file automation/pom.xml
```
- You can use `mvn test` from `automation` to only run tests

### Deploying to Azure Container Registry
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
   docker push serveratg-akabbzauhefhc6ak.azurecr.io/group-e-server:<version>
   ```