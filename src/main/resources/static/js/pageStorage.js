var USER_OPERATION = {
    IN_PROGRESS: 'in_progress',
    COMPLETE: 'complete'
}

var pageStorage = (function() {
    var _step = "playground.step";
    var _projectName = "playground.project.name";
    var _projectGroupId = "playground.project.groupId";
    var _projectArtifactId = "playground.project.artifactId";
    var _projectDescription = "playground.project.description";
    var _projectServices = "playground.project.services";
    var _selectedInfraModules = "playground.selected.infra.modules";
    var _user_operation = USER_OPERATION.IN_PROGRESS;

    var setStep = function(step) {
        localStorage.setItem(_step, step);
    };

    var getStep = function() {
        return localStorage.getItem(_step);
    };

    var setProjectName = function(name) {
        localStorage.setItem(_projectName, name);
    }

    var getProjectName = function() {
        return localStorage.getItem(_projectName);
    }

    var setGroupId = function(groupId) {
        localStorage.setItem(_projectGroupId, groupId);
    }

    var getGroupId = function () {
        return localStorage.getItem(_projectGroupId);
    }

    var setArtifactId = function(artifactId) {
        localStorage.setItem(_projectArtifactId, artifactId);
    }

    var getArtifactId = function () {
        return localStorage.getItem(_projectArtifactId);
    }

    var setDescription = function(description) {
        localStorage.setItem(_projectDescription, description);
    }

    var getDescription = function () {
        return localStorage.getItem(_projectDescription);
    }

    var setMicroServices = function(servicesArray) {
        localStorage.setItem(_projectServices, JSON.stringify(servicesArray));
    }

    var getMicroServices = function () {
        return JSON.parse(localStorage.getItem(_projectServices));
    }

    var setSelectedInfraModules = function(modules) {
        localStorage.setItem(_selectedInfraModules, JSON.stringify(modules));
    }

    var getSelectedInfraModules = function() {
        return JSON.parse(localStorage.getItem(_selectedInfraModules));
    }

    var getUserOperation = function () {
        return _user_operation;
    }

    var setUserOperation = function (operation) {
        _user_operation = operation;
    }

    var setPageStatus = function (pageData) {
        setStep(pageData['step']);
        setProjectName(pageData['name']);
        setGroupId(pageData['groupId']);
        setArtifactId(pageData['artifactId']);
        setDescription(pageData['description']);
        setMicroServices(pageData['microServices']);
        setSelectedInfraModules(pageData['selectedInfraModules']);
    }

    var clearPageStatus = function () {
        var playgroundKeys = [];
        for(var index = 0; index < localStorage.length; index++) {
            if (localStorage.key(index).startsWith('playground.')) {
                playgroundKeys.push(localStorage.key(index));
            }
        }

        playgroundKeys.forEach(function (value) {
            localStorage.removeItem(value);
        });
    }

    return {
        getStep: getStep,
        getProjectName: getProjectName,
        getGroupId: getGroupId,
        getArtifactId: getArtifactId,
        getDescription: getDescription,
        getMicroServices: getMicroServices,
        getSelectedInfraModules: getSelectedInfraModules,
        setPageStatus: setPageStatus,
        clearPageStatus: clearPageStatus,
        getUserOperation: getUserOperation,
        setUserOperation: setUserOperation
    };
})();