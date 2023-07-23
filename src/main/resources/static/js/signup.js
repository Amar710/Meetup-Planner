function noSpace(event) {
    if (event.which === 32)
        event.preventDefault();
}

$("#name").on('change', function() {
    var username = $(this).val();
    console.log("Username: " + username);
    $.ajax({
        url: '/users/exists',
        type: 'GET',
        data: {
            name: username
        },
        success: function(data) {
            console.log("AJAX success");
            if(data) {
                $("#errorMessage").text("Username is already in use").show();
            } else {
                $("#errorMessage").hide();
            }
        }
    });
});

$("#email").on('change', function() {
    var email = $(this).val();
    console.log("Email: " + email);
    $.ajax({
        url: '/users/exists',
        type: 'GET',
        data: {
            type: "email",
            value: email
        },
        success: function(data) {
            console.log("AJAX success");
            if (data) {
                $("#emailErrorMessage").text("Email is already in use").show();
            } else {
                $("#emailErrorMessage").hide();
            }
        }
    });
});





