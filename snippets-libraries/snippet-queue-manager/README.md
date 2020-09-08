# Snippet Queue Manager

The Queue Manager Snippet provides tools to ease the creation, usage and release of queues.
This snippet can manage two different queue types:
  - Queues created from an Excel file.
  - Queues created from a generic source.

### Initialization

To instantiate the class use the constructor provided by each class.

### Main functions
See the main functions of each Queue Manager on his own README:

  - [ExcelQueueManager](./src/main/java/com/appian/rpa/snippets/queuemanager/excel/manager)
  - [GenericQueueManager](./src/main/java/com/appian/rpa/snippets/queuemanager/generic/manager)
 
### Installation

Snippet Queue Manager is distributed using Maven:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets</groupId>
	<artifactId>snippet-queue-manager</artifactId>
	<version>1.4.0</version>
</dependency>
```

### Examples

There are multiple examples to show its usage, as a queue requires a more complex architecture. If the real life scenario requires to process a huge load of items, it would be interesting to split the functionality into two different robotic processes: A [dispatcher (filler)](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-generic-queue-manager-filler), uploading all the elements to be reviewed and a [performer (consumer)](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-generic-queue-manager-consumer), executing the actions needed for each queue element retrieved. Otherwise, an easier implementation (and frequently needed) requires to **create the queue from the rows of an Excel file provided**. The robot uploads and consumes the queue by itself. You can find an example of this configuration [here](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/master/snippets-examples/robot-snippet-excel-queue-manager).
