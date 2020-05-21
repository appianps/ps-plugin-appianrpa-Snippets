![AppianRPA](https://www.appian.com/wp-content/uploads/2020/03/ap_rpa_lockup.png)

## RPA Snippets: An Appian RPA utility, including examples, in order to integrate some robot actions over your process in an easier way. 

<<<<<<< HEAD
### * snippets-libraries, with all the source code.
### * snippets-examples, containing a robot example for each snippet.


### Libraries

#### 1. Credentials Snippet

The CredentialsUtils Class provides the actions to retrieve and manage 
Appian RPA Console Credentials.

#### 2. REST API Snippet

Class to manage the actions referred to a REST API utility

#### 3. Browser Manager Snippet

This utility let the robot manage a web browser (Chrome, Firefox and
IExplorer are supported). Your process should include an instance for each
browser window involved in the robot actions. It also contains a SelectorsManager class, created 
to retrieve all the selectors involved in the robotic process from the selectors.properties file.

#### 4. Application Manager Snippet

Manage the actions referred to a desktop application.

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
=======
TODO: join iomenac snippet commonts and examples as explained below:

### * examples, containing some robots using these snippets.
### * snippet-commons, with all the source code.


### Examples

1. Credentials Snippets Example 

Retrieve three credentials associated to the same application "TEST_ROBOT".

How to use and run the example: 

1. Upload the zip file located in snippets-examples/credentials/console as is. 
It will automatically generate the robotic process with all the requirements needed. 

2. reate three credentials as described: 

TEST_ROBOT 

user1, user2, user3 

password1, password2, password3 (the values are not relevant to understand how these are retrieved).
>>>>>>> PS-460-queueWithoutExcel
