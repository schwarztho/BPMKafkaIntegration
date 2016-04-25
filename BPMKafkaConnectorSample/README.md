# BPMKafkaConnectorSample
 The BPMKafkaConnectorSample folder contains the source code of a sample Java Integration component that allows a BPM process or service to interact with a configured Kafka topic. Currently, this sample allows you to wait for an incoming java message and to get its contents for further processing in BPM. 

# Class BPMKafkaConsumer
This class implements a sample consumer for the messages sent to a Kafka topic.

## Method **configureDetails** (boolean newDebug, int newConsumerTimeoutMs)
Optional method, allows you to configure two settings

Parameters:
* boolean **newDebug**: set to true to enable debug output. Defaults to false.
* int **newConsumerTimeoutMs**: define the timeout period in milliseconds after which the getNextEvent() method returns when there is no new message available on the Kafka topic. This prevents the BPM system to raise a hung thread exception. Defaults to 50000.

## Method getNextEvent(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup)
This method waits for the next incoming message on the configured Kafka topic and returns the message. It waits for at most the timeout configured in the configureDetails() method and returns null if there was no new message to return.

Parameters:
* String **zookeeperUrl**: Connection URL to connect to zookeeper. Typically, this is "localhost:52181".
* String **kafkaTopic**: Kafka topic to listen on. Typically, this is "InfosphereEvents".
* String **kafkaConsumerGroup**: Consumer Group used by this Kafka consumer. Typically, this is "BPMEventConsumerGroup".

Returns:
* String **message**: the new Kafka message, or null if the waiting timed out.

## Method shutdownConsumer(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup, int kafkaNumThreads)
Optional method, allows you to gracefully shutdown the Kafka consumer that gets created under the covers when calling getNextEvent().

Parameters:
* String **zookeeperUrl**: Connection URL to connect to zookeeper. Use same value as for getNextEvent().
* String **kafkaTopic**: Kafka topic to listen on. Use same value as for getNextEvent().
* String **kafkaConsumerGroup**: Consumer Group used by this Kafka consumer. Use same value as for getNextEvent().
* int **kafkaNumThreads**: For now, always provide **1** here.

# How to build the sample yourself
These instructions assume that you have a Java build environment along with GIT and Maven up and running.
* Get all the files from the GIT repository, e.g. via `git clone https://github.com/schwarztho/BPMKafkaIntegration.git`.
* cd into the project folder: `cd BPMKafkaConnectorSample`.
* Compile and build the package: `mvn package`. This will create a file *BPMKafkaConnectorSample-1.0.jar* in the *target* folder.

# How to deploy the sample's .jar file to BPM
* Switch to your BPM Process Designer and open the process app *Kafka Event Processing Sample* in the designer.
* Open the Server File *BPMKafkaConnectorSample-1.0.jar*.
* Click the "Browse..." button, select your new BPMKafkaConnectorSample-1.0.jar file, and then save the Server File.
* Go to the *ProcessKafkaEventsIntoUCSs* Integration Service, and click on the *getNextKafkaMessage* activity.
* In the Properties tab switch to the Definition section and ensure that there is no red X in the section, e.g., next to the Java Class.
* In the Properties tab switch to the Data Mapping section and ensure that all parameters in the Input and Output Mapping are defined.
* Stop any currently running ProcessKafkaEventsIntoUCSs service and start it again.
