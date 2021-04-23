# RPA Snippets: An Appian RPA utility, including examples, in order to integrate some robot actions over your process in an easier way.


These code utilities have been designed to provide an easier implementation of the most frequently used actions in robotic processes, such as an internet navigator or an applications manager to interact with any windows application. 

#### * [snippets-libraries](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries).

#### * [snippets-examples](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples).

## How can I integrate these snippets into my RPA Project?

Once your settings.xml file is successfully configured as explained in the [documentation](https://docs.appian.com/suite/help/20.1/rpa/setup/maven-setup.html),  each of the desired snippets must be previously deployed in the Appian RPA console where the execution is going to be managed. Further help to deploy an Appian RPA source code project can be found [here](https://docs.appian.com/suite/help/20.1/rpa/rpa_in_apps/deploying-apps-rps.html).

After these previous steps, The deployed snippets will be fully available by adding them to your pom.xml project.

The snippets and examples of usage can be found in this repository. All the actions contained and updates are listed below:

### List of Snippets included (latest update 09/2020)


#### 1. [Queue Manager](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/queue-manager).

The Queue Manager provides some utilities to create, update and release a list of elements to be processed by the robot. You can find further information about a queue and its usage [here](https://docs.appian.com/suite/help/20.1/rpa/modules/process-queues-module.html).
An implementation can be found [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-excel-queue-manager)

#### 2. [3270 Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-low-code/snippet-ibm-3270-library).

The Snippet IBM3270 Manager covers some methods to ease the IBM3270 terminal management. Use the low code workflow library and NOT the java snippet / example.

#### 3. [Credentials Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-credentials).

The CredentialsUtils Class provides the actions to retrieve and manage 
Appian RPA Console Credentials. 


#### 4. [Browser Manager Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-browser-manager).

This utility lets the robot manage a web browser (Chrome, Firefox and
IExplorer are supported). Your process should include an instance for each
browser window involved in the robot actions. It also contains a SelectorsManager class, created 
to retrieve all the selectors involved in the robotic process from the selectors.properties file.

We recommend you to read the [Browser Module documentation](https://docs.appian.com/suite/help/20.1/rpa/modules/browser-module.html) to get a better comprehension of this utility before using the snippet.

#### 5. [Application Manager Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-application-manager).

Manage the actions referred to a Windows desktop application.

We recommend you to read the [UIAutomation documentation](https://docs.appian.com/suite/help/20.1/rpa/modules/using-ui-automation.html) to get a better comprehension of this utility before using the snippet.

#### 6. [Instructions Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-instruction). 

Retrieve instructions and environment variables from your Appian RPA Workflow Panel. Find an example [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-instruction).

#### 7. [Email Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-email). 

The email snippet helps you by sending an email from the server. It also lets the user set a variety of different email parameters.  Find an example [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-email).

#### 8. [QR Manager](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-qr-manager). 

Encode & Decode QR Images. You can have a look to its usage in the test cases or the robot example provided [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-QR-manager).

