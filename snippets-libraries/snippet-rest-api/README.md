
## REST API Snippet

Manage the actions referred to a REST API utility.

An integration example can be found [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-rest-api) 


### Snippet method(s):

- **restApiCall()** Call an API Rest endpoint with the given parameters: 
	 * @param consoleUrl URL of the console where the endpoint is hosted
	 * @param endpoint   Endpoint to call
	 * @param apiKey     Console API Key
	 * @param body       Request body

### Installation

The REST API utility can be integrated in your code using Maven. It just has to be included in the ```pom.xml``` file as shown below:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets</groupId>
	<artifactId>snippet-rest-api</artifactId>
	<version>1.0.0</version>
</dependency>
```