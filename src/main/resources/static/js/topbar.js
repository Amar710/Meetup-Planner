// dropbox

// JavaScript to toggle the active class on click
// document.addEventListener("DOMContentLoaded", function() {
//     var dropdowns = document.getElementsByClassName("dropdown");
  
//     for (var i = 0; i < dropdowns.length; i++) {
//       dropdowns[i].addEventListener("click", function(event) {
//         var content = this.querySelector('.dropdown-content');
//         content.style.display = content.style.display === "block" ? "none" : "block";
//         event.stopPropagation(); // Prevent event propagation to the document
//       });
//     }
  
//     document.addEventListener("click", function() {
//       // Hide all dropdowns when user clicks anywhere else on the screen
//       for (var i = 0; i < dropdowns.length; i++) {
//         var content = dropdowns[i].querySelector('.dropdown-content');
//         content.style.display = "none";
//       }
//     });
//   });



// Extract the 'admin' field from the user object
var isAdmin = document.getElementById('user').textContent === 'true'; 

// Get the reference to the "View All" link element
var viewAllLink = document.getElementById('view-all-link');

// Set the visibility based on the 'isAdmin' value
viewAllLink.style.display = isAdmin ? 'block' : 'none';