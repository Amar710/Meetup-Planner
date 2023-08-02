let map, infoWindow;
let directionsService;
let directionsRenderer;
let routeTime = null; 


let params = (new URL(document.location)).searchParams;
let currentLocation = params.get('location');
let decodedString = atob(currentLocation);
let location2 = JSON.parse(decodedString);
let origin = { lat: location2.lat, lng: location2.lng };



function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
    results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function initMap() {
    directionsRenderer = new google.maps.DirectionsRenderer();
    directionsService = new google.maps.DirectionsService();
    map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 49.2786956418124, lng: -122.91971163861588 },
    zoom: 12,
});

infoWindow = new google.maps.InfoWindow();

const locationButton = document.createElement("button");

locationButton.textContent = "Show Current Location";
locationButton.classList.add("custom-map-control-button");
map.controls[google.maps.ControlPosition.TOP_RIGHT].push(locationButton);
locationButton.addEventListener("click", () => {
    if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
        (position) => {
        const pos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
        };

        infoWindow.setPosition(pos);
        infoWindow.setContent("Location found.");
        infoWindow.open(map);
        map.setCenter(pos);
        },
        () => {
        handleLocationError(true, infoWindow, map.getCenter());
        },
    );
    } else {
    handleLocationError(false, infoWindow, map.getCenter());
    }
});

directionsRenderer.setMap(map);
initAutocomplete();
const onChangeHandler = function () {
    calculateAndDisplayRoute(directionsService, directionsRenderer);
};
document.getElementById("pac-input").addEventListener("change", onChangeHandler);



}

// Function to autocomplete the search bar and put a marker down on the given locataion
function initAutocomplete() {
    // Create the search box and link it to the UI element.
    const input = document.getElementById("pac-input");
    const searchBox = new google.maps.places.SearchBox(input);
  
    map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
    // Bias the SearchBox results towards current map's viewport.
    map.addListener("bounds_changed", () => {
      searchBox.setBounds(map.getBounds());
    });
  
    let markers = [];
  
    // Listen for the event fired when the user selects a prediction and retrieve
    // more details for that place.
    searchBox.addListener("places_changed", () => {
      const places = searchBox.getPlaces();
    
      if (places.length == 0) {
          return;
      }
  
      // Store the place_id of the selected place in the pac-input field
      document.getElementById('pac-input').setAttribute('data-place-id', places[0].place_id);
  
  
      // Clear out the old markers.
      markers.forEach((marker) => {
        marker.setMap(null);
      });
      markers = [];
  
      // For each place, get the icon, name and location.
      const bounds = new google.maps.LatLngBounds();
  
      places.forEach((place) => {
        if (!place.geometry || !place.geometry.location) {
          console.log("Returned place contains no geometry");
          return;
        }
  
        const icon = {
          url: place.icon,
          size: new google.maps.Size(71, 71),
          origin: new google.maps.Point(0, 0),
          anchor: new google.maps.Point(17, 34),
          scaledSize: new google.maps.Size(25, 25),
        };
  
        if (place.geometry.viewport) {
          // Only geocodes have viewport.
          bounds.union(place.geometry.viewport);
        } else {
          bounds.extend(place.geometry.location);
        }
      });
      map.fitBounds(bounds);
    });
}

function calculateAndDisplayRoute(directionsService, directionsRenderer) {
    let start = origin; // the origin you have defined previously
    let end = document.getElementById('pac-input').value; // 'pac-input' is your destination input field
  
    directionsService.route(
        {
            origin: start,
            destination: end,
            travelMode: google.maps.TravelMode.WALKING, // Change this to your preferred travel mode
        },
        (response, status) => {
            if (status === 'OK') {
                directionsRenderer.setDirections(response);

                // Extract the route duration and display it on the page
                const routeDuration = response.routes[0].legs[0].duration.text;
                routeTime = response.routes[0].legs[0].duration.value / 60; // duration in minutes

                document.getElementById("route-duration").textContent =
                    "Estimated Duration: " + routeDuration;
            } else {
                window.alert('Directions request failed due to ' + status);
            }
        }
    );
}




document.addEventListener('DOMContentLoaded', (event) => {
  document.getElementById("confirm-location").addEventListener("click", () => {
    const selectedPlace = new google.maps.places.PlacesService(map).getDetails({
        placeId: document.getElementById('pac-input').getAttribute('data-place-id'),
    }, (place, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK) {
            // Send the selected location and route time back to the parent window
            window.opener.postMessage({
              latitude: place.geometry.location.lat(),
              longitude: place.geometry.location.lng(),
              time: routeTime
          }, '*');
          

            // Close the popup window
            window.close();
        }
    });
});

});






window.initMap = initMap;
