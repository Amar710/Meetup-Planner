
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

  app.init();
