![AppianRPA](https://www.appian.com/wp-content/uploads/2020/03/ap_rpa_lockup.png)

## RPA Snippets: An Appian RPA utility, including examples, in order to integrate some robot actions over your process in an easier way.

These code utilities have been designed to provide an easier implementation of the most frequently used actions in robotic processes, such as an internet navigator or an applications manager to interact with any windows application. 

The snippets and examples of usage can be found in this repository. All the actions contained and updates are listed below:

#### * [snippets-libraries](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries).

#### * [snippets-examples](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples).

## How can I integrate these snippets into my RPA Project?

Once your settings.xml file is successfully configured as explained in the [documentation](https://docs.appian.com/suite/help/20.1/rpa/setup/maven-setup.html),  each of the desired snippets must be previously deployed in the Appian RPA console where the execution is going to be managed. Further help to deploy an Appian RPA source code project can be found [here](https://docs.appian.com/suite/help/20.1/rpa/rpa_in_apps/deploying-apps-rps.html).

After these previous steps, The deployed snippets will be fully available by adding them to your pom.xml project.

### List of Snippets currently included (latest update 06/2020)

#### 1. [Credentials Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-credentials).

The CredentialsUtils Class provides the actions to retrieve and manage 
Appian RPA Console Credentials.

#### 2. [REST API Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-rest-api).

The REST API Snippet Class manages the actions referred to a REST API utility.

#### 3. [Browser Manager Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-browser-manager).

This utility let the robot manage a web browser (Chrome, Firefox and
IExplorer are supported). Your process should include an instance for each
browser window involved in the robot actions. It also contains a SelectorsManager class, created 
to retrieve all the selectors involved in the robotic process from the selectors.properties file.

#### 4. [Application Manager Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-application-manager).

Manage the actions referred to a desktop application.

#### 5. [3270 Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-3270-manager).

#### 6. [Instructions Snippet](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/snippet-instruction). 

#### 7. [Queue Manager](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-libraries/queue-manager).

The Queue Manager provides some utilities to create, update and release a list of elements to be processed by the robot. You can find further information about a queue and its usage [here](https://docs.appian.com/suite/help/20.1/rpa/modules/process-queues-module.html).
An implementation can be found [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-excel-queue-manager)


### Examples

#### 1. Credentials Snippet Example 

The Credentials Snippet Example provides a complete usage example from all the
CredentialsUtils snippet methods. The robot will retrieve three existent
credentials (Username + Password) associated to the application "TEST_ROBOT".
In case that these three credentials were not previously created in the
console side, the application will throw an exception.


#### 2. REST API Snippet Example 

The Rest API Robot provides a complete usage example from all the
correspondent snippet methods. Given a queueItem ID, this robotic process
updates the number of attempts to 3. It also set the status as "PENDING". An
error will be thrown if the Item does not exist, or the Status was not
previously marked as "FINISHED_WARN".


#### 3. Browser Manager Snippet Example.

This robotic process has been created to illustrate how the Browser Manager
Snippet should be integrated in your process. It basically shows how a Chrome 
Browser can be easilly opened. Then, the word "Appian" in Google and shows up the 
first result found in the console. An exception will be thrown if any error 
occurs during the process execution.


#### 4. Application Manager Snippet Example. 


The Application Manager Robot provides a complete usage example from all the
correspondent snippet methods. First of all, the notepad editor is opened and
maximized. After that, the Windows native calculator application is opened,
and then, the previous opened notepad is set as foreground application.
Finally, both applicationes are closed. An exception will be thrown if
something goes wrong during any of these actions.

TEST_ROBOT 

user1, user2, user3 

password1, password2, password3 (the values are not relevant to understand how these are retrieved).

