fetch('/api/events/invitations')
    .then(response => response.json()) 
    .then(data => {
        const invitationsDiv = document.querySelector('#invitation');
        data.forEach(event => {
            const eventDiv = document.createElement('div');
            eventDiv.style.border = '1px solid black';
            eventDiv.style.margin = '10px';
            eventDiv.style.padding = '10px';
            eventDiv.style.color = 'white';
            eventDiv.style.backgroundColor = event.color;

            let startDate = new Date(event.start);
            let endDate = new Date(event.end);
            const eventInfo = document.createElement('p');
            eventInfo.textContent = `Event ID: ${event.id}, Name: ${event.text}, Start: ${startDate.toLocaleString()}, End: ${endDate.toLocaleString()}`;

            const location = event.location;
            const locationStr = location ? `Location: ${location.address}` : 'No location';
            const locationInfo = document.createElement('p');
            locationInfo.textContent = locationStr;

            const acceptBtn = document.createElement('button');
            acceptBtn.textContent = 'Accept';
            acceptBtn.onclick = () => acceptInvitation(event.id);

            const declineBtn = document.createElement('button');
            declineBtn.textContent = 'Decline';
            declineBtn.onclick = () => declineInvitation(event.id); // add the decline button

            eventDiv.appendChild(eventInfo);
            eventDiv.appendChild(locationInfo);
            eventDiv.appendChild(acceptBtn);
            eventDiv.appendChild(declineBtn);
            invitationsDiv.appendChild(eventDiv);
        });
    })
    .catch(error => console.error('Error:', error));

function acceptInvitation(eventId) {

    fetch(`/api/userevent/accept/${eventId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ accepted: true }),
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log(data);
        window.location.reload();  // refresh the page
    })
    .catch((error) => console.error('Error:', error));
}

function declineInvitation(eventId) {

    fetch(`/api/events/Remove`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ id: eventId }), // send the eventId in the request body
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        console.log(data);
        window.location.reload();  // refresh the page
    })
    .catch((error) => console.error('Error:', error));
}

  


const datePicker = new DayPilot.Navigator("nav", {
    showMonths: 3,
    skipMonths: 3,
    selectMode: "Week",
    onTimeRangeSelected: args => {
      calendar.update({
        startDate: args.day
      });
      calendar.events.load("/api/events");
    }
  });
  datePicker.init();

  const calendar = new DayPilot.Calendar("dp", {
    eventEndSpec: "Date",
    viewType: "Week",
    headerDateFormat: "dddd MMMM d",
    // produces "Thursday June 17"
    eventHeight: 30,
    eventBarVisible: false,
    onTimeRangeSelected: async (args) => {
      const modal = await DayPilot.Modal.prompt("Create a new event:", "Event");
      calendar.clearSelection();
      if (modal.canceled) {
        return;
      }
      const params = {
        start: args.start,
        end: args.end,
        text: modal.result
      };
      const {data} = await DayPilot.Http.post('/api/events/create', params);
      calendar.events.add(data);
    },
    
    onEventMove: async (args) => {
        const params = {
          id: args.e.id(),
          start: args.newStart,
          end: args.newEnd
        };
        const {data} = await DayPilot.Http.post('/api/events/move', params);
    },
    onEventResize: async (args) => {
        const params = {
          id: args.e.id(),
          start: args.newStart,
          end: args.newEnd
        };
        const {data} = await DayPilot.Http.post('/api/events/move', params);
    },
    onBeforeEventRender: args => {
      const color = args.data.color || "#888888";
      args.data.backColor = DayPilot.ColorUtil.lighter(color);
      args.data.borderColor = "darker";
      args.data.fontColor = "#ffffff";
      
      // Append the location information to the event text
      const location = args.data.location;
      const locationStr = location ? `Address: ${location.address}` : 'No location';
      const eventId = args.data.id;  // Get the event ID
      args.data.html = `Event ID: ${eventId} <br> ${args.data.text}<br>${locationStr}`;
      
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
    
    
    
    contextMenu: new DayPilot.Menu({
      items: [
        {
          text: "more info",
          onClick: async (args) => {
              const e = args.source;
              let info = "";
              info += "Event ID: " + e.id() + "\n";
              info += "Event name: " + e.data.text + "\n";
              if (e.data.location) {
                  info += "Latitude: " + e.data.location.latitude + "\n";
                  info += "Longitude: " + e.data.location.longitude + "\n";
                  info += "Address: " + e.data.location.address + "\n";
              }
      
              // Fetch users related to event and print
              const users = await fetch(`/api/event/${e.id()}/users`).then(response => response.json());
              info += "\n" + 'participants: ';
              // Iterate over users and add them to info string
              users.forEach((user, index) => {
                  info += `${user.name}, `;
              });
      
              console.log(info);
              alert(info);
          }
      },
      
        
        
        {
          text: "-"
        }, 
        {
          text: "Invite",
          onClick: async (args) => {
              const e = args.source;
              try {
                  const response = await DayPilot.Http.get('/api/user/friends');
                  if (response.request.status !== 200) {
                      throw new Error("Failed to retrieve friends");
                  }
                  const friends = response.data; // this assumes the response data is an array of friend names
      
                  // Define the menu items based on the friends list
                  let menuItems = friends.map(friend => ({
                      text: friend,
                      onClick: async () => {
                          const params = {
                              eventId: e.id(),
                              name: friend
                          };
                          try {
                              const inviteResponse = await DayPilot.Http.post('/api/events/invite', params);
                              alert(inviteResponse.data.message);
                          } catch (error) {
                              alert("Failed to invite user: " + error.message);
                          }
                      }
                  }));
      
                  // Create and display the new context menu
                  const friendsMenu = new DayPilot.Menu({items: menuItems});
                  // Show the menu at the cursor position
                  friendsMenu.show(args.source);
              } catch (error) {
                  alert("Failed to retrieve friends list: " + error.message);
              }
          }
      },
      

        {
          text: "rename",
          icon: "icon icon-edit",
          onClick: async (args) => {
            const e = args.source;
            const newName = prompt("Enter the new event name:", e.data.text);
            if (newName !== null) {
              e.data.text = newName;
              calendar.events.update(e);
              const id = e.id();
              const params = {
                text: newName
              };
              const response = await fetch(
                  `/api/events/${id}/update`,
                  { 
                      method: 'POST',
                      headers: { 
                          'Content-Type': 'application/json' 
                      },
                      body: JSON.stringify(params)
                  }
              );
              if(response.ok) {
                const data = await response.json();
                console.log(data.message);
              } else {
                console.log("Response not OK");
              }
            }
          }
      },
      
    {
    text: "Edit Location",
    onClick: async (args) => {
        const e = args.source;

        // Open the "edit-location.html" page in a new window
        const popup = window.open("edit-location.html", "Edit Location", "width=500,height=400");

        // Pass the event ID and current location to the new page
        popup.eventId = e.id();
        popup.currentLocation = e.data.location;

        // Update currentEventId with the selected event's ID
        currentEventId = e.id();

        // Define the message handler
        const messageHandler = async function(event) {
            // Retrieve the updated location from the new page
            const updatedLocation = event.data;

            if (updatedLocation !== undefined) {
                // Update the event's location with the new location
                e.data.location = updatedLocation;
                calendar.events.update(e);

                // Save the updated location to the server
                await saveEventLocation(updatedLocation);

                // Reload the events
                calendar.events.load("/api/events");
            }
        };

        window.removeEventListener('message', messageHandler);
        window.addEventListener('message', messageHandler);
    }
},
{
  text: "Remove",
  onClick: async (args) => {
      const e = args.source;
      const params = {
          id: e.id()
      };

      const {data} = await DayPilot.Http.post('/api/events/Remove', params);
      calendar.events.remove(e);
      calendar.events.load("/api/events"); 
  }
},
      {
        text: "-"
      },      
      {
        text: "Blue",
        icon: "icon icon-blue",
        color: "#3c78d8",
        onClick: (args) => {
          app.updateColor(args.source, args.item.color);
        }
      },
      {
        text: "Green",
        icon: "icon icon-green",
        color: "#13A874",
        onClick: (args) => {
          app.updateColor(args.source, args.item.color);
        }
      },
      {
        text: "Yellow",
        icon: "icon icon-yellow",
        color: "#EFB914",
        onClick: (args) => {
          app.updateColor(args.source, args.item.color);
        }
      },
      {
        text: "Red",
        icon: "icon icon-red",
        color: "#F03030",
        onClick: (args) => {
          app.updateColor(args.source, args.item.color);
        }
      }, {
        text: "defualt",
        color: "#acaab8",
        onClick: (args) => {
          app.updateColor(args.source, args.item.color);
        }
      },

      ]
    })
  });
  calendar.init();

  const app = {
    elements: {
      previous: document.querySelector("#previous"),
      next: document.querySelector("#next"),
    },
    async updateColor(e, color) {
      const params = {
        id: e.id(),
        color: color
      };
      const {data} = await DayPilot.Http.post('/api/events/setColor', params);
      e.data.color = color;
      calendar.events.update(e);
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

      calendar.events.load("/api/events");
    }

    
  };






  window.addEventListener('DOMContentLoaded', (event) => {
    const planButton = document.querySelector("#plan");

    async function fetchEvent(id) {
        const response = await fetch(`/api/events/next/${id}`);

        if (!response.ok) {
            const message = `An error has occurred: ${response.status}`;
            throw new Error(message);
        }

        const data = await response.json();
        return data;
    }


    if (planButton) {
      planButton.addEventListener("click", () => {
        var modal = document.getElementById('eventFormModal');
        modal.style.display = "block";

        const advanceButton = document.querySelector("#advance");
        let updatedLocation = null;

        if (advanceButton) {
          advanceButton.addEventListener("click", async () => {
              // Ask the user for an event ID
              const eventId = prompt("Please enter the event ID");

              // Fetch the event data based on the selected id
              const eventData = await fetchEvent(eventId);

              // Extract the location from the event data and construct LatLng object
              let eventLocation = {
                lat: eventData.location.latitude,
                lng: eventData.location.longitude
              };

              // Open the "advance-mode.html" page in a new window with eventLocation as a query parameter
              const encodedLocation = btoa(JSON.stringify(eventLocation));
              const popup = window.open(`advanceBook.html?location=${encodedLocation}`, "Advance Mode", "width=500,height=400");

              // Pass the LatLng object to the new page
              popup.eventLocation = eventLocation;

              // Define the message handler
              const messageHandler = async function(event) {
                // Retrieve the updated event location from the new page and construct LatLng object
               updatedLocation = {
                  address: event.data.address,
                  lat: event.data.latitude,
                  lng: event.data.longitude,
                  time: event.data.time,
                };

                console.log('Updated location received:', updatedLocation); // This line will print the updatedLocation object

                if (updatedLocation.lat !== undefined && updatedLocation.lng !== undefined) {
                  // Update the event's location with the new LatLng object
                  // Convert the end time of the updated event to a Date object
                  let fetchedEventEndTime = new Date(eventData.end);
                  console.log('Fetched Event End Time:', fetchedEventEndTime); // This line will print the fetchedEventEndTime object

                  // Add the route time (in minutes) to the fetchedEventEndTime
                  fetchedEventEndTime.setMinutes(fetchedEventEndTime.getMinutes() + parseInt(updatedLocation.time));

                  // Adjust for timezone difference
                  let timezoneOffsetMinutes = fetchedEventEndTime.getTimezoneOffset();
                  fetchedEventEndTime.setMinutes(fetchedEventEndTime.getMinutes() - timezoneOffsetMinutes);

                  // Format date as "yyyy-MM-ddThh:mm" 
                  let formattedTime  = fetchedEventEndTime.toISOString().slice(0,16);
                  console.log('Formatted Start Time:', formattedTime); // This line will print the formattedStartTime

                  document.getElementById('start-date-input').value = formattedTime;
                  document.getElementById('end-date-input').value = formattedTime;

          
                  // // Re-initialize the map to use the new origin point.
                  // initMap(eventLocation);
              }
              };

              window.removeEventListener('message', messageHandler);
              window.addEventListener('message', messageHandler);

          });
        }

        

          const createEventButton = document.querySelector("#createEvent");

          if (createEventButton) {
              createEventButton.addEventListener("click", async () => {
                  // Gather the necessary parameters (start, end, text) from the user
                  let start = document.getElementById('start-date-input').value;
                  let end = document.getElementById('end-date-input').value;
                  let text = document.getElementById('event-text-input').value;

                  let params = {
                      start: start,
                      end: end,
                      text: text
                  };

                  if (updatedLocation) {
                    params['location'] = {
                      
                      address: updatedLocation.address,
                      latitude: updatedLocation.lat,
                      longitude: updatedLocation.lng
                    };
                  }

                  try {
                      const response = await fetch('/api/events/create', {
                          method: 'POST',
                          headers: {
                              'Content-Type': 'application/json',
                          },
                          body: JSON.stringify(params),
                      });

                      if (!response.ok) {
                          throw new Error(`HTTP error! status: ${response.status}`);
                      }

                      const data = await response.json();
                      console.log(data);

                      // Close the modal after event creation
                      modal.style.display = "none";

                  } catch (error) {
                      console.log(error);
                  }
              });
          }

          const closeModal = document.querySelector('.close');

          if (closeModal) {
              closeModal.onclick = function () {
                  modal.style.display = "none";
              }
          }

          window.onclick = function (event) {
              if (event.target == modal) {
                  modal.style.display = "none";
              }
          }
      });
    }
});

  

  let currentEventId; // This stores the id of the currently edited event

  async function saveEventLocation(updatedLocation) {
      console.log(`Updating location for event ${currentEventId} to`, updatedLocation);
  
      const response = await fetch(`/api/events/${currentEventId}/location`, {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            latitude: updatedLocation.lat,
            longitude: updatedLocation.lng,
            address: updatedLocation.address, 
        }),
    
      });
  
      if (!response.ok) {
          const message = `An error has occurred: ${response.status}`;
          throw new Error(message);
      }
  
      const data = await response.text();
  
      currentEventId = null; // Clear the current event id after the update
  
      return data ? JSON.parse(data) : {};
  }
  

  window.addEventListener('DOMContentLoaded', (event) => {
    const submitButton = document.getElementById('submitEvent');

    if (submitButton) {
        submitButton.onclick = async function(event) {
            event.preventDefault(); // Prevent the form from submitting normally
      
            // Get the form values
            const start = document.getElementById('start-date-input').value;
            const end = document.getElementById('end-date-input').value;
            const text = document.getElementById('event-text-input').value;
      
            // Build the parameters object
            const params = {
                start: start,
                end: end,
                text: text
            };
      
            // Send the POST request
            const { data } = await DayPilot.Http.post('/api/events/create', params);
      
            // Add the new event to the calendar
            calendar.events.add(data);
      
            // Close the modal
            document.getElementById('eventFormModal').style.display = "none";
        }
    }

    window.onEventSelection = function(id) {
        currentEventId = id; // Update the current event id when an event is selected
    }
});

  
  


  app.init();
