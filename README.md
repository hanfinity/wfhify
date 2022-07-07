# wfhify
network application for WFH/OOO notifications in hybrid work environments

As we adapt to the “new normal”, many companies are embracing a hybrid work model, with employees commuting to the office some days and staying at home other days. Part of the benefit of this model is flexibility, with employees altering their schedule on an ad-hoc basis. With this flexibility comes a need for improved communication.
A remote work status indicator is an internet-connected display board that can be mounted at an employee’s desk. The goal of this device is to allow someone looking for the employee to quickly ascertain whether they’re in the office but away from their desk or working remotely. The employee can use a web-app to set schedules for status changes, manually change the status, set custom messages, and enable/disable the display.
This project consists of a server application running on a Raspberry pi microcontroller with an LED display and a web application client. The client can set a message for the server to display, or schedule a message to display at a certain time. The client can set working hours outside of which the display will show “off work”. 
