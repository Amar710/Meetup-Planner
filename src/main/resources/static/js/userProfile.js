// Get the reference to the second username link by class name
var profileUsernameLink = document.getElementById('username2');

// get user username from server when they log in
var username = 'BrianLam'; // Replace with your variable value

// Set the variable value as the content of the <a> element
profileUsernameLink.textContent = username;