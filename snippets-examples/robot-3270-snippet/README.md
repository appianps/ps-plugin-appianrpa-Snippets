# Snippet 3270 Manager Robot

The IBM3270 Manager Robot provides a complete usage example from all the correspondent snippet methods. It opens an IBM3270 terminal using the emulator indicated as the robot's parameter, goes to a menu page and tries to change the user's password.

Finally the robot closes the emulator.

### Installation

The Snippet 3270 Manager Robot is distributed using Maven:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets.examples</groupId>
	<artifactId>robot-snippet-3270-manager</artifactId>
	<version>0.0.1</version>
</dependency>
```
##### Emulators Installation

This robot allows its execution in two different clients:
- IBM Personal Communications
- WC3270

The following describes how to perform the installation of the emulators and the configuration of a session.

##### wc3270

- Download the emulator from the following url:
http://x3270.bgp.nu/download.html
*(select the stable version from Windows Setup .EXEs)*
- After downloading, run the wc3270 setup wizard.
![Setup Wizard](./console/img/wizard.png)
- Execute a default installation, leave the options suggested by the wizard. In the last screen, select 'Launch Session Wizard' and finish the installation.
![Completed Setup](./console/img/wizard_complete.png)
- On the wc3270 Session Wizard terminal select option 1 to create a new session.
![Session Wizard](./console/img/session_wizard.png)
- Enter the session name, for this robot the session name is **WC3270**.
![Session Wizard - Name](./console/img/session_wizard_name.png)



### Workflow

![Robot workflow](./console/workflow.png)

### Development
You can find the snippet 3270 Manager in the folder snippets-libraries:

https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/PS-430-3270/snippets-libraries/snippet-3270-commons