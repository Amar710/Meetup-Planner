
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
          text: "Delete",
          onClick: async (args) => {
              const e = args.source;
              const params = {
                  id: e.id()
              };
      
              const {data} = await DayPilot.Http.post('/api/events/delete', params);
              calendar.events.remove(e);
              calendar.events.load("/api/events"); 
          }
        },
        {
          text: "invite",
          onClick: async (args) => {
            const e = args.source;
            const uid = prompt("Please enter user id to invite:");
      
            if (uid) {
                const params = {
                    eventId: e.id(),
                    uid: uid
                };
                try {
                    const response = await DayPilot.Http.post('/api/events/invite', params);
                    console.log(response.request.status);
                    if (response.request.status === 200) {
                      alert("Success: " + response.data.message); 
                  } else if (response.request.status === 409) {
                      alert("Conflict: " + response.data.message);
                  } else {
                      alert("Failed to invite user: " + response.data.message);
                  }
                } catch (error) {
                    
                    alert("Failed to send invite: " + error.message);
                }
            } else {
                alert("No user id provided.");
            }
          }
      },
      {
        text: "Edit",
        icon: "icon icon-edit",
        onClick: async (args) => {
          const e = args.source;
          const newName = prompt("Enter the new event name:", e.data.text);
          if (newName !== null) {
            e.data.text = newName;
            calendar.events.update(e);
            const params = {
              id: e.id(),
              text: newName
            };
            const { data } = await DayPilot.Http.post('/api/events/update', params);
            console.log(data.message); // You can handle the response as needed
          }
        }
      },
      // Inside the contextMenu definition, add a new item for the "Edit Location" button
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

            // Remove the event listener after it's done
            window.removeEventListener('message', messageHandler);
        };

        // Remove the previous event listener and add a new one
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
              longitude: updatedLocation.lng
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
  
  function onEventSelection(id) {
      currentEventId = id; // Update the current event id when an event is selected
  }
  
  


  app.init();
