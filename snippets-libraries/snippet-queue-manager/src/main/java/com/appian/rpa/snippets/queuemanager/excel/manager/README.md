# Snippet Excel Queue Manager

Snippet Excel Queue Manager is a simple project to facilitate the management of queues created from an Excel file. If there is a preselected queue, the robot executes it.

Important. The anotations used on the object model must match the Excel column title.

It is also important to note that to run several robots with the same in-process queue, it is necessary to create an optional parameter in the robot's configuration called 'numberOfExecutions' with a default value of 1.

# Initialization
To instantiate the class use the constructor provided by the class.
The constructor returns an instance of the ExcelQueueManager class.

# Main functions
For queues:
  - Create a queue from an Excel file or use the preselected queue. You can also pass a File object as method parameter.
  - Extract the queue Excel file.
  - Extract the queue output Excel file.
  - Close queue.
   
For queue items:
  - Save the new value of an item.
  - Update a queue item.
  - Get the next queue item.

Utils:
  - Get the current queue.

### Installation

Snippet Excel Queue Manager is distributed using Maven:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets</groupId>
	<artifactId>queue-manager</artifactId>
	<version>1.2.0</version>
</dependency>
```

### Examples
You can find an example of how to use this snippet in a robotic process in the folder snippets-examples:

Robot Snippet Excel Queue Manager:

[robot-snippet-excel-queue-manager](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-excel-queue-manager)



