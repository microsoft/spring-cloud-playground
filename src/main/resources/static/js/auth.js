(function () {

    // Enter Global Config Values & Instantiate ADAL AuthenticationContext
    window.config = {
        instance: 'https://login.microsoftonline.com/',
        tenant: 'common',
        clientId: 'f94305db-2a5f-4701-9fb8-996eebf9659b',
        postLogoutRedirectUri: window.location.origin,
        cacheLocation: 'localStorage', // enable this for IE, as sessionStorage does not work for localhost.
    };
    var authContext = new AuthenticationContext(config);

    var $freeAccountLink = $('#free_link');
    var $signInButton = $("#login_link");
    var $signOutButton = $("#logout_link");
    var $userDropdown = $("#user_dropdown");
    var $loggedUser = $("#logged_user");
    var $errorMessage = $(".app-error");

    // Check For & Handle Redirect From AAD After Login
    var isCallback = authContext.isCallback(window.location.hash);
    authContext.handleWindowCallback();
    $errorMessage.html(authContext.getLoginError());

    if (isCallback && !authContext.getLoginError()) {
        window.location = authContext._getItem(authContext.CONSTANTS.STORAGE.LOGIN_REQUEST);
    }

    // Check Login Status, Update UI
    var user = authContext.getCachedUser();
    if (user) {
        $freeAccountLink.addClass("hidden");
        $signInButton.addClass("hidden");
        $userDropdown.removeClass("hidden");
        $loggedUser.text(user.userName);
    } else {
        $freeAccountLink.removeClass("hidden");
        $signInButton.removeClass("hidden");
        $userDropdown.addClass("hidden");
        $loggedUser.text(undefined);
    }

    // Register NavBar Click Handlers
    $signOutButton.click(function () {
        authContext.logOut();
    });
    $signInButton.click(function () {
        authContext.login();
    });
}());
