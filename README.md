# BPMKafkaIntegration Sample
The BPMKafkaIntegration Sample project shows how to launch an IBM BPM Business Process Definition
triggered by a Kafka message. This allows to use a Kafka topic as an inbound message queue and to react on 
incoming messages in BPM. The dispatcher logic deciding what to do with each message runs in a
General System Service that can be easily adapted to suit your needs.

## Sample Structure
The [KafkaEventProcessingSample](KafkaEventProcessingSample) folder contains
the sample process in a .twx file. In this example, the **ReviewEventProcess** BPD gets launched for every incoming
governance event issued by the IBM InfoSphere Information Governance Catalog when a governance asset like a
category, term, governance rule, or governance policy gets created, modified, or deleted. The integration service 
**ProcessKafkaEventsIntoUCAs** connects to the configured Kafka topic via a Java Integration activity based on the
the BPMKafkaConnectorSample. The source code for the BPMKafkaConnectorSample is provided in the 
[BPMKafkaConnectorSample](BPMKafkaConnectorSample) folder. The General System Service **IssueUCA** contains the logic
that decides which Undercover Agents to issue for each incoming message.

## System Requirements
This sample is based on the following components
* IBM Business Process Manager Standard 8.5.0.1
* IBM InfoSphere Information Server 11.5 with [Governance Rollup 1](http://www-01.ibm.com/support/docview.wss?uid=swg24041824) or IBM InfoSphere Information Server 11.5.0.1 (contains Rollup 1)
* Apache Kafka 0.8.2.1

## Installation and Configuration
This section describes how to setup the sample process. This description references the following installation directories:
* **$IS_install_path$**: Installation folder for the IBM InfoSphere Information Server
* **$BPM_install_path$**: Installation folder for the IBM Business Process Manager Standard
* **$Kafka_install_path$**: Installation folder of the Apache Kafka package. Governance Rollup 1 installs this to *$IS_install_path$/shared-open-source/kafka/install/*

### Setting up the Information Server side
* Follow the instructions on [Managing InfoSphere Information Server events with Apache Kafka](http://www-01.ibm.com/support/docview.wss?uid=swg21977431) to get started with Apache Kafka based Event processing in the InfoSphere Information Server
* Note down the zookeeper connection string. Typically, this is "$yourServerName$:52181"
* Note down the Kafka topic. Typically, this is "InfosphereEvents"

### Setting up the BPM side
* Copy the Kafka libraries into your BPM environment  
  Copy all .jar files from the *$Kafka_install_path$/libs/* folder into the *$BPM_install_path$/v8.5/lib/ext/* folder.
* Import the .twx file located in the [KafkaEventProcessingSample](KafkaEventProcessingSample) folder into your BPM Process Designer.
* Open the "Kafka Event Processing Sample" Process App in the Designer.
* Open the "ProcessKafkaEventsIntoUCAs" Integration Service.
* Change the default values for the variables "zookeeperUrl" and "kafkaTopic" to the values you just noted down above.
* Run the "ProcessKafkaEventsIntoUCAs" Integration Service. This will start listening for new events on the Kafka topic.
* Launch the Process Portal of your BPM system to see the new tasks created by the sample ReviewEventProcess.
* Launch the IBM InfoSphere Information Governance Catalog and perform a change on one of the existing governance assets like Category, Term, Governance Rule, or Governance Policy, or create a new one. Then watch a new ReviewEvent task appear in the Process Portal.

### Shutting down the "ProcessKafkaEventsIntoUCAs" Integration Service
Follow this procedure to shut down the "ProcessKafkaEventsIntoUCAs" Integration Service:
* Launch the Process Admin Console of your BPM system
* Go to Monitoring / Process Monitor
* Click on the "Services" button
* In the list "Active Services Currently Executing" click on "ProcessKafkaEventsIntoUCAs"
* Click on the "Halt Service" button.
 
### Extending the sample
This sample can be easily extended to process any event type issued by the IBM InfoSphere Information Server as well as to process messages from other sources. For this, have a look at these places:
* Add a new Undercover Agents to the IIS Events Toolkit. UCAs need to reside in a toolkit when the BPD to be triggered is in another Process App.
* Extend the logic in the "IssueUCA" General System Service to process the new types of incoming messages and to issue the new UCA.
* Create a new Business Process Definition, change the type of the start activity to Message (Implementation/Start Event Details), and attach it to your new UCA.


## Modifying the BPMKafkaConnectorSample
See the README.md file in the [BPMKafkaConnectorSample](BPMKafkaConnectorSample) folder.
