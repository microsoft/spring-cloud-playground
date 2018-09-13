# Azure Spring Cloud Playground
Azure Spring Cloud Playground helps you scaffold and generate Microservice projects.
It provides you with native Spring Cloud modules as well as modules developed for
connecting to and consuming Azure related services. The generated Microservice projects
follow [The Twelve-Factor App](https://12factor.net/) methodology and are ready for further development with [Azure Dev Spaces](https://docs.microsoft.com/en-us/azure/dev-spaces/index) and [Azure Kubernetes Service](https://azure.microsoft.com/en-us/services/container-service/).

# Getting Started
* Install [Maven](https://maven.apache.org/install.html) and [Docker](https://docs.docker.com/install/)
* Access to [azure-spring-cloud.azurewebsites.net](https://azure-spring-cloud.azurewebsites.net/)
* Sign in with your Azure subscription if you want your Spring Cloud Azure modules to be configured. If you don't have a Azure subscription get a [free one](https://azure.microsoft.com/en-us/free/).
* Select the modules you'd like to start with and generate the project.
* You can run locally with docker compose. Extract and go to project root directory, run the following:
```
cd docker
run.cmd (run.sh)
```
* After all microservices start, access gateway endpoint at http://localhost:9999

# Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
