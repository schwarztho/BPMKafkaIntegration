/*
 * Copyright IBM Corp. 2016
 * 
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages. 
 */
package com.ibm.is.StewardshipCenterSamples.BPMKafkaIntegrationSample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

public class BPMKafkaConsumer {
	
	private static final String KAFKA_TOPIC = "test";
	private static final String KAFKA_ZOOKEEPER_URL = "keenan.boeblingen.de.ibm.com:2181";
	private static final String KAFKA_CONSUMER_GROUP = "BPMEventConsumerGroup";
	private static final int KAFKA_NUMTHREADS = 1;
	private static final int KAFKA_CONSUMER_TIMEOUT_MS = 50000;
	
	/**
	 * don't access directly! Always use getKafkaConsumer() to obtain a reference!
	 */
//    private static ConsumerConnector consumerConn = null;
    private static Map<String, ConsumerConnector> consumerConnByKey = new HashMap<String, ConsumerConnector>();
    
    /**
     * Stores a set of streamKeys for each consumerConnector (by consumerKey)
     */
    private static Map<String, Set<String>> consumerConnUsageTracker = new HashMap<String, Set<String>>();
	
//	private static KafkaStream<byte[], byte[]> sharedKafkaStream = null;
	private static Map<String, KafkaStream<byte[], byte[]>> sharedKafkaStreamByKey = new HashMap<String, KafkaStream<byte[], byte[]>>();
	
	private static int kafkaConsumerTimeoutMs = KAFKA_CONSUMER_TIMEOUT_MS;
	private static boolean debug = false;
	
	
	public static void configureDetails(boolean newDebug, int newConsumerTimeoutMs) {
		debug = newDebug;
		kafkaConsumerTimeoutMs = newConsumerTimeoutMs;
	}
    
    
	public static String getNextEvent(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup) {
		logDebug("BPMKafkaConsumer.getNextEvent() -- start: " + kafkaTopic + "@" + zookeeperUrl + " - " + kafkaConsumerGroup);
		int numThreads = KAFKA_NUMTHREADS;
		String message = null;
		KafkaStream<byte[], byte[]> kafkaStream = getKafkaStream(zookeeperUrl, kafkaTopic, kafkaConsumerGroup, numThreads);
		logDebug("BPMKafkaConsumer.getNextEvent() -- gotKafkaStream");
	    ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
	    try {
	    	if (it.hasNext()) {
	    		MessageAndMetadata<byte[], byte[]> msgAndMeta = it.next();
	    		message = new String(msgAndMeta.message());
	    		long offset = msgAndMeta.offset();
	    		logDebug("KafkaMessage #" + offset + ": " + message);
	    	} else {
	    		logDebug("No new KafkaMessage!");
	    	}
	    } catch (ConsumerTimeoutException e) {
	    	logDebug("No new KafkaMassage received within timeout period.");
	    }
	    return message;	}
	
	
	private static String getNextEvent() {
		return getNextEvent(KAFKA_ZOOKEEPER_URL, KAFKA_TOPIC, KAFKA_CONSUMER_GROUP);
	}

	private static KafkaStream<byte[], byte[]> getKafkaStream(
			String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup,
			int kafkaNumThreads) {
		String streamKey = buildKafkaStreamKey(zookeeperUrl, kafkaTopic,
				kafkaConsumerGroup, kafkaNumThreads);
		KafkaStream<byte[], byte[]> kafkaStream = null;
		synchronized (sharedKafkaStreamByKey) {
			kafkaStream = sharedKafkaStreamByKey.get(streamKey);
			if (kafkaStream == null) {
				Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
				topicCountMap.put(kafkaTopic, new Integer(kafkaNumThreads));
				ConsumerConnector consumer = getKafkaConsumer(zookeeperUrl,
						kafkaConsumerGroup);
				logDebug("BPMKafkaConsumer.getKafkaStream() -- gotKafkaConsumer");
				Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
						.createMessageStreams(topicCountMap);
				List<KafkaStream<byte[], byte[]>> streams = consumerMap
						.get(kafkaTopic);
				for (final KafkaStream<byte[], byte[]> stream : streams) {
					// just return the first stream for now.
					// TODO: keep track of multiple streams...
					kafkaStream = stream;
					sharedKafkaStreamByKey.put(streamKey, kafkaStream);
					addStreamToConsumerUsage(buildKafkaConsumerKey(zookeeperUrl, kafkaConsumerGroup), streamKey);
					break;
				}
				if (streams.size() > 1) {
					logWarn("Topic "
							+ kafkaTopic
							+ " has "
							+ streams.size()
							+ " streams, but we are looking only at the first one!");
				}
				if (kafkaStream == null) {
					logError("BPMKafkaConsumer.getKafkaStream(): no stream for topic "
							+ kafkaTopic);
					// TODO Deal with error case that there is no stream.
				}
			}
		}
		return kafkaStream;
	}


	private static String buildKafkaStreamKey(String zookeeperUrl, String kafkaTopic,
			String kafkaConsumerGroup, int kafkaNumThreads) {
		return "" + zookeeperUrl + "##" + kafkaTopic + "##" + kafkaConsumerGroup + "##" + kafkaNumThreads;
	}

	
	private static ConsumerConnector getKafkaConsumer(String zookeeperUrl,
			String kafkaConsumerGroup) {
		String consumerKey = buildKafkaConsumerKey(zookeeperUrl,
				kafkaConsumerGroup);
		ConsumerConnector consumerConn = null;
		synchronized (consumerConnByKey) {
			consumerConn = consumerConnByKey.get(consumerKey);
			logDebug("BPMKafkaConsumer.getKafkaConsumer() -- before createConsumerConfig(), consumerConn="
					+ consumerConn);
			if (consumerConn == null) {
				consumerConn = kafka.consumer.Consumer
						.createJavaConsumerConnector(createConsumerConfig(
								zookeeperUrl, kafkaConsumerGroup));
				consumerConnByKey.put(consumerKey, consumerConn);
			}
		}
		logDebug("BPMKafkaConsumer.getKafkaConsumer() -- after createJavaConsumerConnector(), consumerConn="
				+ consumerConn);
		return consumerConn;
	}
	
	
	private static String buildKafkaConsumerKey(String zookeeperUrl, String kafkaConsumerGroup) {
		return "" + zookeeperUrl + "##" + kafkaConsumerGroup;
	}
	
	
    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("consumer.timeout.ms", "" + kafkaConsumerTimeoutMs);
        
        return new ConsumerConfig(props);
    }
    
    
	
	private static void addStreamToConsumerUsage(String consumerKey, String streamKey) {
		Set<String> usageSet = null;
		usageSet = consumerConnUsageTracker.get(consumerKey);
		if (usageSet == null) {
			usageSet = new HashSet<String>();
			consumerConnUsageTracker.put(consumerKey, usageSet);
		}
		usageSet.add(streamKey);
	}

	
	private static boolean removeStreamFromConsumerUsage(String consumerKey, String streamKey) {
		boolean isLastUsage = false;
		Set<String> usageSet = null;
		usageSet = consumerConnUsageTracker.get(consumerKey);
		if (usageSet != null) {
			usageSet.remove(streamKey);
			isLastUsage = usageSet.isEmpty();
		}
		return isLastUsage;
	}
	
    private static void shutdownConsumer() {
    	shutdownConsumer(KAFKA_ZOOKEEPER_URL, KAFKA_TOPIC, KAFKA_CONSUMER_GROUP, KAFKA_NUMTHREADS);
    }

    public static void shutdownConsumer(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup,
			int kafkaNumThreads) {
		logDebug("BPMKafkaConsumer.shutdownConsumer() -- start: " + kafkaTopic + "@" + zookeeperUrl + " - " + kafkaConsumerGroup);

		String streamKey = buildKafkaStreamKey(zookeeperUrl, kafkaTopic,
				kafkaConsumerGroup, kafkaNumThreads);
		KafkaStream<byte[], byte[]> kafkaStream = null;
		synchronized (sharedKafkaStreamByKey) {
			kafkaStream = sharedKafkaStreamByKey.get(streamKey);
			if (kafkaStream != null) {
				sharedKafkaStreamByKey.remove(streamKey);
				logDebug("BPMKafkaConsumer.shutdownConsumer() -- removed stream " + streamKey);
			}
			
			String consumerKey = buildKafkaConsumerKey(zookeeperUrl,
					kafkaConsumerGroup);
			boolean isLastUsage = removeStreamFromConsumerUsage(consumerKey, streamKey);
			if (isLastUsage) {
				ConsumerConnector consumerConn = null;
				synchronized (consumerConnByKey) {
					consumerConn = consumerConnByKey.get(consumerKey);
					if (consumerConn != null) {
						consumerConn.shutdown();
						consumerConnByKey.remove(consumerKey);
						logDebug("BPMKafkaConsumer.shutdownConsumer() -- shutdown " + consumerKey);
					}
				}
			}
		
		}
		logDebug("BPMKafkaConsumer.shutdownConsumer() -- end.");
    }

    
    
    public static void main(String[] args) {
    	logDebug("Testing BPMKafkaConsumer...");
    	String nextMessage = getNextEvent();
    	logDebug("NextMessage = " + nextMessage);
    	shutdownConsumer();
    	logDebug("Finished cleanup!");
    }
    
    
    private static void logDebug(String msg) {
    	// TODO: switch to LOGGER
    	if (debug) {
    		System.out.println("DEBUG: " + msg);
    	}
    }
    
    private static void logWarn(String msg) {
    	// TODO: switch to LOGGER
    	System.out.println("WARN: " + msg);
    }

    private static void logError(String msg) {
    	// TODO: switch to LOGGER
    	System.err.println("ERROR: " + msg);
    }
}
