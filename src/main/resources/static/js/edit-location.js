let map, infoWindow;
let selectedLocation = null;

function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 49.2786956418124, lng: -122.91971163861588 },
        zoom: 12,
    });

    infoWindow = new google.maps.InfoWindow();

    // Create the search box and link it to the UI element
    const input = document.getElementById("pac-input");
    const searchBox = new google.maps.places.SearchBox(input);

    map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    map.addListener("bounds_changed", () => {
        searchBox.setBounds(map.getBounds());
    });

    // Listen for the event fired when the user selects a prediction and retrieve more details for that place
    searchBox.addListener("places_changed", () => {
        const places = searchBox.getPlaces();

        if (places.length === 0) return;

        // For each place, get the icon, name, and location
        places.forEach((place) => {
            if (!place.geometry || !place.geometry.location) {
                console.log("Returned place contains no geometry");
                return;
            }

            // Store the selected location's latitude and longitude
            selectedLocation = {
                lat: place.geometry.location.lat(),
                lng: place.geometry.location.lng(),
                address: place.formatted_address,
            };
        

            // Update the "pac-input" value with the formatted address
            document.getElementById("pac-input").value = place.formatted_address;

            console.log(selectedLocation); // This should print the selected location
        });
    });

    document.getElementById("confirm-location").addEventListener("click", () => {
        // Send the selected location back to the parent window
        window.opener.postMessage(selectedLocation, '*');

        // Close the popup window
        window.close();
    });
}

window.initMap = initMap;