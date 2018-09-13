$(function () {
    var allServiceList = new microservices([]);

    if (navigator.appVersion.indexOf("Mac") != -1) {
        $(".btn-primary").append("<kbd>&#8984; + &#9166;</kbd>");
    }
    else {
        $(".btn-primary").append("<kbd>alt + &#9166;</kbd>");
    }

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
    var infraPort = $(".port-input");

    // Configurable steps
    var metaDataConfig = $("#meta-data-config");
    var infraModulesSelector = $("#infra-selection");
    var azureModulesSelector = $("#azure-selection");
    var metaDataStep = $("#meta-data-step")[0];
    var infraStep = $("#infra-step")[0];
    var azureStep = $("#azure-step")[0];

    var selectedModules = $("#selected-modules-list");
    var createAzureServiceBtn = $("#create-azure-service");

    // Checkbox
    var infraCheckbox = $(".infra-checkbox");
    var azureCheckbox = $(".azure-checkbox");

    var azureServiceNameInput = $("#azure-service-name");
    var azureServicePortInput = $("#azure-service-port");

    var serviceNameHelp = $("#service-name-help");
    var servicePortHelp = $("#port-help");

    var inProgressLabel = $("#in-progress");
    var generateSucceedLabel = $("#generate-succeed");
    var generateFailedLabel = $("#generate-failed");

    var githubModal = $("#github-login-modal");
    var githubModalClose = $("#github-login-modal .delete");

    var githubConfigModal = $("#github-config-modal");
    var githubConfigModalClose = $("#github-config-modal .delete");
    var cancelGithubModal = $("#cancel-github-push");

    azureCheckbox.on("change", addServiceBtnChecker);
    azureServiceNameInput.on("input", addServiceBtnChecker);
    azureServicePortInput.on("input", addServiceBtnChecker);

    configPort.on("click", function () {
        if (infraPort.hasClass("hidden")) {
            infraPort.addClass("is-active");
            infraPort.removeClass("hidden");
            configPort.text("hide service configuration?")
        } else {
            infraPort.removeClass("is-active");
            infraPort.addClass("hidden");
            configPort.text("configure your services?")
        }
    });

    infraPort.on("blur", function() {
        updateInfraPort($(this));
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

    $("#next-goto-infra-step").on("click", function() {
        showInfraModulesConfig();
    });

    $("#next-goto-azure-step").on("click", function() {
       showAzureModulesConfig();
    });

    $("#prev-goto-meta-step").on("click", function() {
        showMetaDataConfig();
    });

    $("#prev-goto-infra-step").on("click", function() {
       showInfraModulesConfig();
    });

    infraCheckbox.on("change", function() {
        updateInfraService($(this), true);
    });

    createAzureServiceBtn.on("click", function() {
        var serviceName = azureServiceNameInput.val().trim();
        var servicePort = azureServicePortInput.val().trim();

        var azureModuleCheckboxs = $("input[name='azure-modules']");
        var moduleList = [];
        azureModuleCheckboxs.each(function() {
            if($(this)[0].checked) {
                moduleList.push($(this).val());
            }
        })

        var azureMicroService = new microservice(serviceName, moduleList, servicePort, true);
        if(addServiceOnPage(azureMicroService, true)) {
            // Clear input values
            azureServiceNameInput.val("");
            azureServicePortInput.val("");
            azureModuleCheckboxs.each(function() {
                $(this).prop('checked', false);
            });
        }
    });

    $("#download-project").on("click", function(event) {
        generateInProgress();
        var data = getProjectData();
        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            var a;
            if (xhttp.readyState === 4 && xhttp.status === 200) {
                var fileName = getAttachmentName(xhttp);

                if (window.navigator && window.navigator.msSaveOrOpenBlob) { // For IE
                    window.navigator.msSaveOrOpenBlob(xhttp.response, fileName);
                } else { // For non-IE
                    a = document.createElement('a');
                    a.href = window.URL.createObjectURL(xhttp.response);
                    a.download = fileName;
                    a.style.display = 'none';
                    document.body.appendChild(a);
                    a.click();
                    a.remove();
                }

                generateSucceed();
            } else if (xhttp.readyState === 4 && xhttp.status !== 200) {
                generateFailed(xhttp.response);
            }
        };

        xhttp.open("POST", '/project.zip');
        xhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");
        setCsrfHeader(xhttp);
        // Set responseType as blob for binary responses
        xhttp.responseType = 'blob';
        xhttp.send(JSON.stringify(data));

        event.preventDefault();
    });

    $("#push-to-github-btn").on("click", function(event) {
        event.preventDefault();

        var data = getProjectData();
        var repoName = $("#github-config-modal input").val();
        if (!repoName || repoName.trim().length === 0 || /\s/.test(repoName.trim())) {
            return;
        }
        data['repoName'] = repoName.trim();

        generateInProgress();
        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            if (xhttp.readyState === 4 && xhttp.status === 201) {
                var repoUrl = xhttp.getResponseHeader("Location");
                generateSucceed();
                generateGithubUrl(repoUrl);
            } else if (xhttp.readyState === 4) {
                generateFailed(xhttp.response);
            }
            closeModal(githubConfigModal);
        };

        xhttp.open("POST", '/push-to-github');
        xhttp.setRequestHeader("Content-Type", "application/json; charset=utf-8");

        setCsrfHeader(xhttp);
        xhttp.send(JSON.stringify(data));
    });

    $(".push-to-github").on("click", function() {
        if (!hasLoggedIn()) {
            showModal(githubModal);
            event.preventDefault();
            return;
        }

        showModal(githubConfigModal);
    });

    $("#generate-project").on("click", function() {
       event.preventDefault();
    });

    $("#open-download").on("click", function () {
        $("#generate-project").toggleClass('is-active');
        event.preventDefault();
        event.stopPropagation();
    });

    function setCsrfHeader(xhttp) {
        var csrfToken = $("input[name='_csrf']").val();
        var csrfTokenHeader = $("input[name='_csrf_header']").val();

        xhttp.setRequestHeader(csrfTokenHeader, csrfToken);
    };

    function getProjectData() {
        var groupId = $("#groupId").val();
        var artifactId = $("#artifactId").val();
        var projectName = $("#project-name").val();
        var description = $("#description").val();

        return {
            name: projectName,
            groupId: groupId,
            artifactId: artifactId,
            baseDir: artifactId,
            description: description,
            packageName: groupId + "." + artifactId,
            microServices: allServiceList.serviceList
        };
    }

    function isValidServiceName(serviceName) {
        return serviceName && /^([a-zA-Z0-9\-]*)$/.test(serviceName);
    }

    function isValidPort(port) {
        return port && !isNaN(port) && port > 0;
    }

    function getAttachmentName(xhttprequest) {
        var disposition = xhttprequest.getResponseHeader('content-disposition');
        var matches = /"([^"]*)"/.exec(disposition);
        return (matches != null && matches[1] ? matches[1] : 'demo.zip');
    }

    function showMetaDataConfig() {
        toggleElements([metaDataConfig], [infraModulesSelector, azureModulesSelector]);

        activateStep(metaDataStep);
        disActivateStep(infraStep);
        disActivateStep(azureStep);

        showCompleteForm();
    }

    function showInfraModulesConfig() {
        toggleElements([infraModulesSelector], [metaDataConfig, azureModulesSelector]);

        completeStep(metaDataStep);
        activateStep(infraStep);
        disActivateStep(azureStep);

        showCompleteForm();
    }

    function showAzureModulesConfig() {
        toggleElements([azureModulesSelector], [metaDataConfig, infraModulesSelector]);

        completeStep(metaDataStep);
        completeStep(infraStep);
        activateStep(azureStep);

        showCompleteForm();
    }

    function showCompleteForm() {
        $("#form div")[0].scrollIntoView(false);
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
        if(!isValidServiceName(service.getName()) || !isValidPort(service.getPort())
            || typeof service.getModuleList() === 'undefined' || service.getModuleList().length === 0) {
            console.warn("Some service property is empty or format illegal, " + JSON.stringify(service));
            return false;
        }

        allServiceList.addService(service);
        // Append selected services into the list on the page
        selectedModules.append(serviceItemDom(service));
        createAzureServiceBtn.prop('disabled', true);

        if (service.isDeletable()) {
            $("#" + service.getName() + " span").on("click", function () {
                deleteServiceOnPage(service.getName());
                $("input[value='" + service.getName() + "']").prop('checked', false);
            });
        }
        return true;
    }

    function deleteServiceOnPage(serviceName) {
        allServiceList.deleteServiceByName(serviceName);
        $("#selected-modules-list #" + serviceName).remove();
    }

    function serviceItemDom(service) {
        var serviceElement = '<li id=\"' + service.getName() + '\">';
        if (service.isDeletable()) {
            serviceElement += '<span class="icon"><i class="fas fa-times"></i></span>';
        } else {
            serviceElement += '<span class="icon" title="Cannot delete"><i class="fas fa-info-circle"></i></span>'
        }

        return serviceElement + '<strong>' + service.getName() + '</strong>, module(s): '
            + service.getModuleList().toString() + ', port: ' + service.getPort() + '</li>';
    }

    function addServiceBtnChecker() {
        var azureModuleSelected = false;
        azureCheckbox.each(function() {
            azureModuleSelected = azureModuleSelected || $(this)[0].checked;
        });

        var serviceName = azureServiceNameInput.val().trim();
        var servicePort = azureServicePortInput.val().trim();

        checkAndShowHelpMsg('name', serviceName, isValidServiceName, serviceNameHelp);
        checkAndShowHelpMsg('port', servicePort, isValidPort, servicePortHelp);

        if(azureModuleSelected && serviceNameHelp.is(":hidden") && servicePortHelp.is(":hidden")) {
            createAzureServiceBtn.prop('disabled', false);
        } else {
            createAzureServiceBtn.prop('disabled', true);
        }
    }

    function checkAndShowHelpMsg(prop, value, checkRule, helpElement) {
        var matchedServices = allServiceList.serviceList.filter(function(service) {
            return service[prop] === value;
        });

        if(!$.isEmptyObject(matchedServices) || !checkRule(value)) {
            showElements([helpElement]);
        } else {
            hideElements([helpElement]);
        }
    }

    function updateInfraPort(portInput) {
        var infraCheckbox = portInput.prev("input");
        var serviceName = infraCheckbox.val();

        if (infraCheckbox[0].checked) {
            deleteServiceOnPage(serviceName);
            updateInfraService(infraCheckbox, false);
        }
    }

    function updateInfraService(infraCheckbox, serviceDeletable) {
        var serviceName = infraCheckbox.val();
        var moduleList = [serviceName];
        var port = infraCheckbox.next("input").val();

        var service = new microservice(serviceName, moduleList, port, serviceDeletable);
        if(infraCheckbox[0].checked) {
            addServiceOnPage(service);
        } else {
            deleteServiceOnPage(serviceName);
        }
    }

    function generateInProgress() {
        toggleElements([inProgressLabel], [generateSucceedLabel, generateFailedLabel]);
    }

    function generateSucceed() {
        removeGithubUrl();
        toggleElements([generateSucceedLabel], [inProgressLabel, generateFailedLabel]);
    }

    function generateFailed(failureMessage) {
        toggleElements([generateFailedLabel], [inProgressLabel, generateSucceedLabel]);
        generateFailedLabel.find("span").append(failureMessage);
    }

    function showModal(modalElement) {
        modalElement.addClass("is-active");
    }

    function closeModal(modalElement) {
        modalElement.removeClass("is-active");
    }

    githubModalClose.on("click", function(event) {
       event.preventDefault();
       closeModal(githubModal);
    });

    githubConfigModalClose.on("click", function(event) {
        event.preventDefault();
        closeModal(githubConfigModal);
    });

    cancelGithubModal.on("click", function(event) {
        event.preventDefault();
        closeModal(githubConfigModal);
    });

    function generateGithubUrl(url) {
        if (!url || url.trim().length === 0) {
            return;
        }
        removeGithubUrl();
        generateSucceedLabel.append("<p> Available at <a href=\"" + url + "\" target=\"_blank\">Github repository</a> </p>")
    }

    function removeGithubUrl() {
        generateSucceedLabel.find("p").remove();
    }

    function getCurrentStep() {
        if($("#azure-step").hasClass("is-active")) {
            return 3;
        } else if($("#infra-step").hasClass("is-active")) {
            return 2;
        }
        return 1;
    }

    function setActiveStep(stepNumber) {
        if(stepNumber === "1") {
            showMetaDataConfig();
        } else if(stepNumber === "2") {
            showInfraModulesConfig();
        } else {
            showAzureModulesConfig();
        }

    }

    function setInputValue(inputElement, value) {
        if(value && !/\s/.test(value)) {
            inputElement.val(value);
        }
    }

    function getSelectedInfraModules() {
        var selectedModules = [];
        infraCheckbox.each(function() {
            if($(this)[0].checked) {
                selectedModules.push($(this).val())
            }
        });

        return selectedModules;
    }

    function checkInfraModules(infraModules) {
        if (!Array.isArray(infraModules) || !infraModules.length) {
            return;
        }

        $.each(infraModules, function(index, moduleName) {
            infraCheckbox.filter(function() {
                return this.value === moduleName;
            }).prop('checked', true);
        });
    }

    $(document).on("click", function (event) {
        $(".dropdown").each(function () {
            $(this).removeClass('is-active');
        });
        event.stopPropagation();
    });

    $(window).on('beforeunload', function(){
        var pageStatus = getProjectData();
        pageStatus['step'] = getCurrentStep();
        pageStatus['selectedInfraModules'] = getSelectedInfraModules();

        pageStorage.setPageStatus(pageStatus);
    });

    $(window).on('load', function(){
        setActiveStep(pageStorage.getStep());
        setInputValue($("#project-name"), pageStorage.getProjectName());
        setInputValue($("#groupId"), pageStorage.getGroupId());
        setInputValue($("#artifactId"), pageStorage.getArtifactId());
        setInputValue($("#description"), pageStorage.getDescription());
        checkInfraModules(pageStorage.getSelectedInfraModules());

        var storedServices = pageStorage.getMicroServices();
        if (Array.isArray(storedServices) && storedServices.length) {
            // Load stored microservices from localstorage
            $.each(storedServices, function(index, service) {
                addServiceOnPage(new microservice(service['name'], service['modules'], service['port'], service['deletable']));
            });
        } else {
            // Initialize already selected infra services
            infraCheckbox.each(function(){
                updateInfraService($(this), false);
            });
        }
    });
});