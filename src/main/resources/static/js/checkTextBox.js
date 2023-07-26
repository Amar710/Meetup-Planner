function validateEmail(email) 
{

    // Regular expression for email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    
    // Check if the email matches the regular expression
    return emailRegex.test(email);
}

function checkForSpaces(str) {

    // Check if the string contains any spaces
    return str.includes(' ');
}


function validate(){

    // get all of the values in the text box
    var email = document.getElementById("email").value;
    var name = document.getElementById("name").value;
    var password = document.getElementById("password").value;
    var confirm = document.getElementById("confirm").value;

    // check for email validation
    if(!emailValidation(email)) 
    {
        alert("Please enter a proper email")
        document.getElementById("email").focus()
        return false;
    }

    // check if name is empty 
    if(name.trim() == "")
    {
        alert("Please enter a name")
        document.getElementById("name").focus()
        return false;
    }

    // check if there is spaces in the name
    if(checkForSpaces(name))
    {
        alert("Your name contains space")
        document.getElementById("name").focus()
        return false;
    }

    // check if password is empty
    if(password.trim() == "")
    {
        alert("Please enter a password")
        document.getElementById("password").focus()
        return false;

    }

    // check if password has spaces
    if(checkForSpaces(password))
    {
        alert("Your password contains space")
        document.getElementById("password").focus()
        return false;
    }

    // check if confirmation is empty
    if(confirm.trim() == "")
    {
        alert("Please confirm your password")
        document.getElementById("confirm").focus()
        return false;
    }

    // check if the password and the confirmation are the same
    if(confirm.trim() != password.trim())
    {
        alert("Your password confirmation doesn't match with your password")
        document.getElementById("confirm")
        return false;
    }

    return true
}


