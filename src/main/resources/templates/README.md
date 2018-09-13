# Getting Started

## Requirements
Install [Maven](https://maven.apache.org/install.html) and [Docker](https://docs.docker.com/install/).

## Configuration
Configuration Azure service module from the [spring-cloud-azure-samples](https://github.com/Microsoft/spring-cloud-azure/tree/master/spring-cloud-azure-samples).

## Run
#### Docker-compose
```
cd docker && run.cmd (run.sh)
```

#### Local machine
```
mvn clean package
```
After build success, run the jar with the sequences in below:
* Config server
* Eureka server
* Hystrix dashboard if exist
* Any other Azure service module
* Gateway
  