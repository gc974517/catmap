# Development Thinktank

Issues, ideas, and meeting times (etc.) are listed here. This document is subject to (massive) changes, so check often for updates.

### Scrum:

Quality scrum hours are tentative, but we intend to follow the schedule

* Wednesdays 6:00PM - 7:30PM
* Thursdays 3:39PM - 4:39PM
	- **Note:** Only if time allows or if needed. We all have very busy schedules.

**Additional Meetings:**

None currently scheduled. Live demo on Tuesday, __10/22/19__!

### Past Meetings:

* Wednesday, 09/18: 6:00PM - 7:30PM
	- Discuss project ideas and nail down a concept for the app.
	- Divide team into frontend and backend developers.
		+ Frontend: Marcelo & Ian
		+ Backend: Garett & Daniel
	- Determine possible toolchain to begin development.
		+ Java (frontend) & Python (backend)
		+ Android Studio
		+ Wireshark
		+ To determine: helpful APIs/libraries.

* Wednesday, 09/25: 6:00PM - 7:30PM
	- Frontend (Marcelo & Ian):
		+ Discuss ideas for app design.
		+ Start making mockups!
		+ How can we integrate Google Maps API?
	- Backend (Garett & Daniel):
		+ Start looking into Python libraries for networking. (Specifically, can we ping Wi-Fi extenders reliably?)
		+ How can we integrate our backend scripts with the Java frontend? (Libraries?)

* Wednesday, 10/02: 6:00PM - 10:00PM
	- Frontend (Marcelo & Ian):
		+ Continue with design ideas & mockups.
		+ Google Maps API integrated. How can we implement indoor maps?
		+ Assuming we can, how do we integrate path finding?
	- Backend (Garett & Daniel):
		+ Experiment with Python code for Multiping library.
		+ Learn more about the Kivy library for app integration.

* Wednesday, 10/09: 6:00PM - 7:30PM
	- Switch development focus from Java / Python to pure Java.
	- Learn Indoor Atlas SDK & Google Maps API integration.
	- Frontend (Marcelo & Ian):
		+ Map Stocker Center (1st Floor) [Marcelo].
		+ Continue considering possible app designs (hopefully start building soon!) [Ian].
	- Backend (Garett & Daniel):
		+ Learn some of the gritty details of IA's implementation. (Their docs are pretty good.)
		+ Continue to play around with Java & Python code just in case IA becomes unusable.

* Monday, 10/14: 7:00PM - 8:30PM
	- Group programming & design session.
	- Successful testing of location tracking & wayfinding examples.
	- Team organizaiton changes:
		+ Frontend (Design & Features): Ian & Daniel
		+ Backend (SDK & Implementation): Marcelo & Garett
	- Work on a functional design for the app, then we can import working SDK code into the main project.
	- Discuss ideas for future features.
		+ Destination searching (Ian & Daniel).
		+ Automatic region detection & wayfinding (Marcelo & Garett).
		+ Detecting user orientation (Marcelo & Garett).
		+ Make it look good (Ian & Daniel).

* Wednesday, 10/16: 7:00PM - 8:00PM
	- Group programming & design session.
	- IndorAtlas API Documentation reading session.(Garett & Marcelo)
		+ Start Implementation of API from scratch in CatMap.
	- Design and Overview of App. (Daniel & Ian)

* Sunday, 10/20: 8:00PM - 3:00AM (Monday)
	- Group programming session.
	- IndorAtlas API & Google Maps API Implementation (Garett & Marcelo)
		+ Start of full implementation.
		+ Debugging errors.
		+ Power Outage at Stocker Center.
		+ Google Maps API key not working.
	- Design and Overview of App (Daniel & Ian)
		+ Creating OU Color Scheme.
		+ Putting search bar for future use.
		+ Paw button for future use.
		+ Working towards front end.
		+ Studying IndorAtlas functions to find implementation for  homescreen.

* Monday, 10/21 4:00pm-7:00pm
	- Final debugging session before live demo.
		+ GoogleMaps API fixed.
		+ Wayfinding now works.
	- Creating PowerPoint for live demo.

* Wednesday,10/23 6:00pm-6:30pm
	- Discuss live demo feedback.
		+ Possible additions to make to app.
	- Setting new goals for each person.
		+ Garett: Refine region detection between indoors and outdoors, alongside orientation detection.
		+ Marcelo: Labelling rooms inside buildings for future search function, mapping out other floors(2,3, ground).
		+ Ian: Utilize paw button for wayfinding.
		+ Daniel: Creating a working search bar.
