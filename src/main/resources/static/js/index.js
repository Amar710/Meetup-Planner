
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
    eventHeight: 30,
    eventBarVisible: false,
    onTimeRangeSelected: async (args) => {
      // Store the selected start and end dates for later use
      document.getElementById('start-date-input').value = args.start;
      document.getElementById('end-date-input').value = args.end;
    
      // Open the modal
      document.getElementById('eventFormModal').style.display = "block";
    
      calendar.clearSelection();
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
                              if (inviteResponse.request.status === 200) {
                                  alert("Success: " + inviteResponse.data.message);
                              } else if (inviteResponse.request.status === 409) {
                                  alert("Conflict: " + inviteResponse.data.message);
                              } else {
                                  alert("Failed to invite user: " + inviteResponse.data.message);
                              }
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
        text: "Auto",
        color: "auto",
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

            if (advanceButton) {
              advanceButton.addEventListener("click", async () => {
                  // Ask the user for an event ID
                  const eventId = prompt("Please enter the event ID");

                  // Fetch the selected event from the backend
                  const event = await fetchEvent(eventId);

                  // Log the ID, text (name), and location of the event to the console
                  console.log(`ID for event: `, eventId);
                  console.log(`Text (name) for event ID ${eventId}: `, event.text);
                  console.log(`Location data for event ID ${eventId}: `, event.location);
                  console.log(`End time for event ID ${eventId}: `, event.end); // Log the end time
            
                  // Convert the end time of the fetched event to the required format and set it as the start time of the new event
                  // const fetchedEventEndTime = new Date(event.end);

                  const fetchedEventEndTime = new Date(event.end + 'Z');
                  const formattedEndTime = fetchedEventEndTime.toISOString().slice(0,16);
                  document.getElementById('start-date-input').value = formattedEndTime;
                  
                  // console.log(`Local end time for event ID ${eventId}: `, localEndTime);

                  console.log(`Converted end time for event ID ${eventId}: `, fetchedEventEndTime);
                  console.log(`Converted end time for event ID ${eventId}: `, formattedEndTime);

                  document.getElementById('start-date-input').value = formattedEndTime;
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
