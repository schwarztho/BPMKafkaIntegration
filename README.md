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

## Installation and Configuration

## Modifying the BPMKafkaConnectorSample

