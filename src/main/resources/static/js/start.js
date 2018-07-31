$(function () {
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

    function microservice(serviceName, moduleList, port) {
        this.name = serviceName;
        this.modules = moduleList;
        this.port = port;
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

    var allServiceList = new microservices([]);

    if (navigator.appVersion.indexOf("Mac") != -1) {
        $(".btn-primary").append("<kbd>&#8984; + &#9166;</kbd>");
    }
    else {
        $(".btn-primary").append("<kbd>alt + &#9166;</kbd>");
    }

    $("#free_link").on("click", function() {
        $.get("/free-account");
    });

    $("#login_link").on("click", function() {
        $.get("/login-account");
    });


    $("#type").on('change', function () {
        $("#form").attr('action', $(this.options[this.selectedIndex]).attr('data-action'))
    });

    // Switch between get started video tabs
    var generateTab = $("#generate-tab");
    var buildRunTab = $("#build-run-tab");
    var generateVideo = $("#generate-animation");
    var buildRunVideo = $("#build-run-animation");

    // Switch between configuration and default
    var configPort = $("#config-port");
    var port = $(".port-input");

    // Modules selection elements
    var infraModulesSelector = $("#infra-selection");
    var azureModulesSelector = $("#azure-selection");
    var nextStepButton = $("#next-step");
    var prevStepButton = $("#previous-step");
    var infraStep = $("#infra-step")[0];
    var azureStep = $("#azure-step")[0];

    var selectedModules = $("#selected-modules-list");
    var createAzureServiceBtn = $("#create-azure-service");

    var serviceForm = $("#form");

    // Checkbox
    var infraCheckbox = $(".infra-checkbox");

    configPort.on("click", function () {
        if (port.hasClass("hidden")) {
            port.addClass("is-active");
            port.removeClass("hidden");
            configPort.text("reset to default configuration ?")
        } else {
            port.removeClass("is-active");
            port.addClass("hidden");
            configPort.text("configure your services ?")
        }
    });

    generateTab.on("click", function() {
        generateTab.addClass("is-active");
        buildRunTab.removeClass("is-active");

        generateVideo.removeClass("hidden");
        buildRunVideo.addClass("hidden");

    });

    buildRunTab.on("click", function() {
        buildRunTab.addClass("is-active");
        generateTab.removeClass("is-active");

        generateVideo.addClass("hidden");
        buildRunVideo.removeClass("hidden");
    });

    nextStepButton.on("click", function() {
       showAzureModules();
       completeStep(infraStep);
       activateStep(azureStep);
    });

    prevStepButton.on("click", function() {
       showInfraModules();
       disActivateStep(azureStep);
       activateStep(infraStep);
    });

    infraCheckbox.on("change", function() {
        var serviceName = $(this).val();
        var moduleList = [serviceName];
        var port = $(this).next("input").val();

        var service = new microservice(serviceName, moduleList, port);
        if($(this)[0].checked) {
            addServiceOnPage(service);
        } else {
            deleteServiceOnPage(serviceName);
        }
    });

    createAzureServiceBtn.on("click", function() {
        var serviceName = $("#azure-service-name").val();
        var servicePort = $("#azure-service-port").val();

        var azureModuleCheckboxs = $("input[name='azure-modules']");
        var moduleList = [];
        azureModuleCheckboxs.each(function() {
            if($(this)[0].checked) {
                moduleList.push($(this).val());
            }
        })

        var azureMicroService = new microservice(serviceName, moduleList, servicePort);
        if(addServiceOnPage(azureMicroService)) {
            // Clear input values
            $("#azure-service-name").val("");
            $("#azure-service-port").val("");
            azureModuleCheckboxs.each(function() {
                $(this).prop('checked', false);
            });
        }
    });

    serviceForm.submit(function(event) {
        var csrfToken = $("input[name='_csrf']").val();
        var csrfTokenHeader = $("input[name='_csrf_header']").val();
        var groupId = $("#groupId").val();
        var artifactId = $("#artifactId").val();

        var data = {
            name: artifactId,
            groupId: groupId,
            artifactId: artifactId,
            baseDir: artifactId,
            packageName: groupId + "." + artifactId,
            microServices: allServiceList.serviceList
        };

        $.ajax({
            type: "POST",
            url: "/project.zip",
            data: JSON.stringify(data),
            beforeSend: function(request) {
                request.setRequestHeader(csrfTokenHeader, csrfToken);
            },
            success: console.log("post success"),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        });

        event.preventDefault();
    });

    function showInfraModules() {
        infraModulesSelector.removeClass("hidden");
        azureModulesSelector.addClass("hidden");
    }

    function showAzureModules() {
        infraModulesSelector.addClass("hidden");
        azureModulesSelector.removeClass("hidden");
    }

    function activateStep(stepElement) {
        stepElement.className = "step-item is-active";
    }

    function disActivateStep(stepElement) {
        stepElement.className = "step-item";
    }

    function completeStep(stepElement) {
        stepElement.className = "step-item is-completed is-success";
    }

    function addServiceOnPage(service) {
        if(!service.getName() || !service.getPort() || isNaN(service.getPort())
            || typeof service.getModuleList() === 'undefined' || service.getModuleList().length === 0) {
            console.warn("Some service property is empty or format illegal, " + JSON.stringify(service));
            return false;
        }

        allServiceList.addService(service);
        // Append selected services into the list on the page
        selectedModules.append(serviceItemDom(service));
        $("#" + service.getName() + " a").on("click", function(){
            deleteServiceOnPage(service.getName());
            $("input[value='" + service.getName() + "']").prop('checked', false);
        });
        return true;
    }

    function deleteServiceOnPage(serviceName) {
        allServiceList.deleteServiceByName(serviceName);
        $("#selected-modules-list #" + serviceName).remove();
    }

    function serviceItemDom(service) {
        return '<li id=\"' + service.getName() + '\"><a class=\"delete\"></a><strong>' + service.getName() +
            '</strong>, modules: ' + service.getModuleList().toString() +
            ', port: ' + service.getPort() + '</li>';
    }
});