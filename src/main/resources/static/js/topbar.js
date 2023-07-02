
//username
// Get the reference to the <a> element
var usernameLink = document.getElementById('username-link');

// get user username from server when they log in
var username = 'BrianLam'; // Replace with your variable value

// Set the variable value as the content of the <a> element
usernameLink.textContent = username;



// dropbox

// JavaScript to toggle the active class on click
document.addEventListener("DOMContentLoaded", function() {
var dropdowns = document.getElementsByClassName("dropdown");

for (var i = 0; i < dropdowns.length; i++) {
    dropdowns[i].addEventListener("click", function() {
    var content = this.querySelector('.dropdown-content');
    content.style.display = content.style.display === "block" ? "none" : "block";
    });
}
});

// create function that will set showViewAll
// get userid and check if admin approval if so set this to true
// if plain user set to false
var showViewAll = false; // Replace with your boolean value


// Get the reference to the "View All" link element
var viewAllLink = document.getElementById('view-all-link');

// Set the visibility based on the boolean value
viewAllLink.style.display = showViewAll ? 'block' : 'none';