function main() {
    // Get the value of the profile UID from the data attribute
    var userIdElement = document.querySelector('#ViewForm').dataset.userId;
    
    // Declare profileUid variable before if statement
    var profileUid;
  
    // Ensure the userId is a valid number
    if (userIdElement && !isNaN(userIdElement)) {
        profileUid = parseInt(userIdElement, 10);
    } else {
        console.error('Invalid userId:', userIdElement);
        // Handle the error or exit the function to prevent further execution
        return;
    }

    console.log('ProfileUid:', profileUid);
  
const datePicker = new DayPilot.Navigator("nav", {
  showMonths: 3,
  skipMonths: 3,
  selectMode: "week",
  onTimeRangeSelected: args => {
    calendar.update({
      startDate: args.day
    });
    calendar.events.load(`/api/events/${profileUid}`);
  }
});
datePicker.init();

const calendar = new DayPilot.Calendar("dp", {
  eventEndSpec: "Date",
  viewType: "Week",
  headerDateFormat: "dddd MMMM d",
  eventHeight: 30,
  eventBarVisible: false,
  timeRangeSelectedHandling: "Disabled", 
  eventMoveHandling: "Disabled", 
  eventResizeHandling: "Disabled", 
  onBeforeEventRender: args => {
    const color = args.data.color || "#888888";
    args.data.backColor = DayPilot.ColorUtil.lighter(color);
    args.data.borderColor = "darker";
    args.data.fontColor = "#ffffff";
  
    // Append the location information to the event text
    const location = args.data.location;
    const locationStr = location ? `Lat: ${location.latitude}, Lng: ${location.longitude}` : 'No location';
    args.data.html = `${args.data.text}<br>${locationStr}`;
  
    args.data.areas = [
      {
        top: 6,
        right: 6,
        width: 18,
        height: 18,
        icon: "icon-triangle-down",
        visibility: "Visible",
        action: "ContextMenu",
        style: "font-size: 12px; background-color: #fff; border: 1px solid #ccc; padding: 2px 2px 0px 2px; cursor:pointer; box-sizing: border-box; border-radius: 15px;"
      }
    ];
  },  
});
calendar.init();

const app = {
  elements: {
    previous: document.querySelector("#previous"),
    next: document.querySelector("#next"),
  },
  init() {
    app.elements.previous.addEventListener("click", () => {
      const current = datePicker.selectionDay;
      datePicker.select(current.addHours(-168));
    });
    app.elements.next.addEventListener("click", () => {
      const current = datePicker.selectionDay;
      datePicker.select(current.addHours(168));
    });

    calendar.events.load(`/api/events/${profileUid}`);
  }
};

function getReverseGeocodingData(lat, lng) {
  const apiKey = 'AIzaSyCY2fd9kd3KWm-lclnFOJzmVe_ozqHbuqg'; // Replace with your actual API Key
  const url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${apiKey}`;
  
  fetch(url)
  .then(response => response.json())
  .then(data => {
      if(data.status === "OK") {
          // The formatted_address field will have the complete address
          return data.results[0].formatted_address;
      } else {
          throw new Error(`Geocode error: ${data.status}`);
      }
  })
  .catch(error => console.error('Error:', error));
}


app.init();

}

main();

