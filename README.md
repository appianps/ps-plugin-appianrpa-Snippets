![AppianRPA](https://www.appian.com/wp-content/uploads/2020/03/ap_rpa_lockup.png)

## RPA Snippets: An Appian RPA utility, including examples, in order to integrate some robot actions over your process in an easier way. 

TODO: join iomenac snippet commonts and examples as explained below:

### * snippets-examples, containing a robot example for each snippet.
### * snippets-libraries, with all the source code.


### Examples

1. Credentials Snippets Example 

Retrieve three credentials associated to the same application "TEST_ROBOT".

How to use and run the example: 

- Upload the zip file located in snippets-examples/credentials/console as is. 
It will automatically generate the robotic process with all the requirements needed. 

- Create three credentials as described: 

TEST_ROBOT 

user1, user2, user3 

password1, password2, password3 (the values are not relevant to understand how these are retrieved).


2. REST API Example 

The Rest API Robot provides a complete usage example from all the
correspondent snippet methods. Given a queueItem ID, this robotic process
updates the number of attempts to 3. It also set the status as "PENDING". An
error will be thrown if the Item does not exist, or the Status was not
previously marked as "FINISHED_WARN".


3. Browser Manager

This robotic process has been created to illustrate how the Browser Manager
Snippet should be integrated in your process. It basically shows how a Chrome 
Browser can be easilly opened. Then, the word "Appian" in Google and shows up the 
first result found in the console. An exception will be thrown if any error 
occurs during the process execution.


4. Application Manager. 


The Application Manager Robot provides a complete usage example from all the
correspondent snippet methods. First of all, the notepad editor is opened and
maximized. After that, the Windows native calculator application is opened,
and then, the previous opened notepad is set as foreground application.
Finally, both applicationes are closed. An exception will be thrown if
something goes wrong during any of these actions.
