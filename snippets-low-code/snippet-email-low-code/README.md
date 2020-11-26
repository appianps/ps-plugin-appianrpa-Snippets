## Email Snippet

This snippet helps you on send email's from the server. You can configure a lot of email parameters. 

An integration example can be found [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-email)

### Installation

The Email utilities can be integrated in your code using Maven. It just has to be included in the ```pom.xml``` file as shown below:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets</groupId>
	<artifactId>snippet-email</artifactId>
	<version>1.1.0</version>
</dependency>
```

### Important things to know before use this snippet
1. It is important to correctly configure the SMTP server beforehand. Please, set an username and a password on the RPA console SMTP server configuration.
2. You can only fill in the body of the email in one way: either using a static string with the ```.body(String)``` method, or using a Velocity template with the method ```velocityConfiguration(File, Map<String, Object>)```.
For more information about Velocity, please visit the [documentation page](https://velocity.apache.org/engine/2.2/getting-started.html).
3. You can set the server configuration in two exclusive ways: either using the SMTP server of the RPA console using ```.useServerConguration(true)```, or setting the host and port manually using ```.host(String)``` and ```.port(int)```. Manual configuration only works if usingServerConfiguration is not true.

