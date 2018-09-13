function microservices(serviceList) {
    this.serviceList = serviceList;
}

microservices.prototype.addService = function(microservice) {
    this.serviceList.push(microservice);
}

microservices.prototype.deleteServiceByName = function(serviceName) {
    this.serviceList = this.serviceList.filter(function(service) {
        return service.name != serviceName;
    });

    return this.serviceList;
}

function microservice(serviceName, moduleList, port, deletable) {
    this.name = serviceName;
    this.modules = moduleList;
    this.port = port;
    this.deletable = deletable;
}

microservice.prototype.getName = function() {
    return this.name;
}

microservice.prototype.getModuleList = function() {
    return this.modules;
}

microservice.prototype.getPort = function() {
    return this.port;
}

microservice.prototype.isDeletable = function() {
    return this.deletable;
}
