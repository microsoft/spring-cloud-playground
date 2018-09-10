(function() {
    var $signInButton = $("#login_link");
    var $signOutButton = $("#logout_link");
    var $userDropdown = $("#user_dropdown");
    var $loggedUser = $("#logged_user");

    $signOutButton.on("click", function() {
        logout();
    });

    function logout() {
        var csrfToken = $("input[name='_csrf']").val();
        var csrfTokenHeader = $("input[name='_csrf_header']").val();

        var xhttp = new XMLHttpRequest();
        
        xhttp.onreadystatechange = function () {
            if(this.readyState == XMLHttpRequest.DONE && this.status == 200) {
                loggedOutSuccess();
            }
        }

        xhttp.open("POST", '/logout');
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhttp.setRequestHeader(csrfTokenHeader, csrfToken);
        xhttp.send(null);
    }

    function loggedOutSuccess() {
        $signInButton.removeClass("hidden");
        $userDropdown.addClass("hidden");
        $loggedUser.text(undefined);
    }
}())