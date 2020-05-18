# Snippet Generic Queue Manager

Snippet Generic Queue Manager is a simple project to facilitate the management of queues created from a generic source.

# Main functions
For queues:
  - Create and assign new queue.
  - Assign an existing queue if exists.
  - Close queue.
   
For queue items:
  - Add an item to the queue.
  - Update a queue item.
  - Find list of items by their key.
  - Get the next queue item.

Utils:
  - Get the current queue.
  - Get the current queue manager.

### Installation

Snippet Generic Queue Manager is distributed using Maven:
```xml
<dependency>
	<groupId>com.appian.rpa.snippets</groupId>
	<artifactId>queue-manager</artifactId>
	<version>1.1.0</version>
</dependency>
```

### Development
You can find two examples of how to use this snippet in a robotic process in the folder snippets-examples:

Robot that creates and fill the queue:

[robot-snippet-generic-queue-manager-filler](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/PS-460-queueWithoutExcel/snippets-examples/robot-snippet-generic-queue-manager-filler)

Robot that process the queue and updates the items:

[robot-snippet-generic-queue-manager-consumer](https://github.com/appianps/ps-plugin-appianrpa-Snippets/tree/PS-460-queueWithoutExcel/snippets-examples/robot-snippet-generic-queue-manager-consumer)



