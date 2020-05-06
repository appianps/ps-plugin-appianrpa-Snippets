![AppianRPA](https://www.appian.com/wp-content/uploads/2020/03/ap_rpa_lockup.png)

## RPA Snippets: An Appian RPA utility, including examples, in order to integrate some robot actions over your process in an easier way. 

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


2. REST API Example 

Update a queue Item identified by its ID Number as "PENDING" if the previous Status was "FINISHED_WARN". 
The robot also gives three new attempts to the item to be finished. An error will be thrown if the Item does not exist
or simply if the Status is different from "FINISHED_WARN"