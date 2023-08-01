// Function to prevent spaces in input fields
function noSpace(event) {
    if (event.which === 32)
      event.preventDefault();
  }
  
  // Username validation
  $("#name").on('change', function() {
    var username = $(this).val().toLowerCase();
    console.log("Username: " + username);
    $.ajax({
      url: '/users/exists',
      type: 'GET',
      data: {
        type: "name",
        value: username
      },
      success: function(data) {
        console.log("AJAX success");
        if (data) {
          $("#usernameErrorMessage").text("Username is already in use").show();
        } else {
          $("#usernameErrorMessage").hide();
        }
      }
    });
  });
  
  // Email validation
  $("#email").on('change', function() {
    var email = $(this).val().toLowerCase();
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
  
  // Form submission with validations
  $("#signupForm").submit(function(event) {
    var form = this;
    var email = $("#email").val();
    var username = $("#name").val();
    // Perform AJAX validations for both email and username
    $.ajax({
      url: '/users/exists',
      type: 'GET',
      data: {
        type: "email",
        value: email
      },
      success: function(emailData) {
        console.log("AJAX success for email");
        if (emailData) {
          $("#emailErrorMessage").text("Email is already in use").show();
          event.preventDefault();
        } else {
          // Email is valid, check for username
          $.ajax({
            url: '/users/exists',
            type: 'GET',
            data: {
              type: "name",
              value: username
            },
            success: function(usernameData) {
              console.log("AJAX success for username");
              if (usernameData) {
                $("#usernameErrorMessage").text("Username is already in use").show();
                event.preventDefault();
              } else {
                // Username is valid, allow form submission
                $("#usernameErrorMessage").hide();
                form.submit();
              }
            },
            error: function() {
              console.log("AJAX error for username");
              event.preventDefault();
            }
          });
        }
      },
      error: function() {
        console.log("AJAX error for email");
        event.preventDefault();
      }
    });
  
    event.preventDefault();
  });
  

