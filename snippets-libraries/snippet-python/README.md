# Snippet Python


Run a Python Script using Appian RPA. 

## Requirements

A Python version and your custom script dependencies must be installed in the target resource. You also require to write the Python script according to the version rules. 
We recommend to use Python 3.x at least, as Python 2.x is not being supported anymore.

[Anaconda](https://docs.anaconda.com/anaconda/install/) is the easiest way to install Python 3.x into your resource. It will provide you the Anaconda Prompt, 
where you can easily install your custom python libraries and packages.

Once you install Anaconda, the default Python path is "C:\\Users\\{resource.username}\\Anaconda3\\python". This will be required to initialize the PythonUtils Object.

Example:

PythonUtils pythonUtils = new PythonUtils("C:\\Users\\resource.username\\Anaconda3\\python")

## Utilities:

* runScript(String scriptPath, String scriptParameters). Given a Python script path, 
it will execute the script using the Python version previously given during the Object initialization.

Please, review the unit tests provided or the [example robot](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-python) to get more details about how are these methods used. 

## How to install 
```
<dependency>
    <groupId>com.appian.rpa.snippets</groupId>
    <artifactId>snippet-python</artifactId>
    <version>1.0.0</version>
</dependency>
```
