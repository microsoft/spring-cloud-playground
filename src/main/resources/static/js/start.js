$(function () {
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
    });

    prevStepButton.on("click", function() {
       showInfraModules();
    });

    function showInfraModules() {
        infraModulesSelector.removeClass("hidden");
        azureModulesSelector.addClass("hidden");
    }

    function showAzureModules() {
        infraModulesSelector.addClass("hidden");
        azureModulesSelector.removeClass("hidden");
    }
});
