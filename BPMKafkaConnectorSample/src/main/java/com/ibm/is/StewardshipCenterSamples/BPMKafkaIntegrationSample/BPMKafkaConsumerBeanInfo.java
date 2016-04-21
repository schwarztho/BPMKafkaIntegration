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

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

public class BPMKafkaConsumerBeanInfo extends SimpleBeanInfo {
	@SuppressWarnings("rawtypes")
	private Class beanClass = BPMKafkaConsumer.class;

	@Override
	public MethodDescriptor[] getMethodDescriptors() {
		try {
			// public static void configureDetails(boolean newDebug, int newConsumerTimeoutMs) {
			MethodDescriptor methodDescriptor1 = getMethodDescription(
					"configureDetails",
					new String[] { "newDebug (boolean)", "newConsumerTimeoutMs (int)" },
					new Class[] { boolean.class, int.class });

			// public static String getNextEvent(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup) {
			MethodDescriptor methodDescriptor2 = getMethodDescription(
					"getNextEvent", new String[] { "zookeeperUrl (String)", "kafkaTopic (String)", "kafkaConsumerGroup (String)" },
					new Class[] { String.class, String.class, String.class });

			// public static void shutdownConsumer(String zookeeperUrl, String kafkaTopic, String kafkaConsumerGroup, int kafkaNumThreads)
			MethodDescriptor methodDescriptor3 = getMethodDescription(
					"shutdownConsumer", new String[] { "zookeeperUrl (String)", "kafkaTopic (String)", "kafkaConsumerGroup (String)", "kafkaNumThreads (int)" },
					new Class[] { String.class, String.class, String.class, int.class });

			return new MethodDescriptor[] { methodDescriptor1,
					methodDescriptor2, methodDescriptor3 };
		} catch (Exception e) {
			return super.getMethodDescriptors();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MethodDescriptor getMethodDescription(String methodName,
			String parameters[], Class classes[]) throws NoSuchMethodException {
		MethodDescriptor methodDescriptor = null;
		Method method = beanClass.getMethod(methodName, classes);

		if (method != null) {
			ParameterDescriptor paramDescriptors[] = new ParameterDescriptor[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				ParameterDescriptor param = new ParameterDescriptor();
				param.setShortDescription(parameters[i]);
				param.setDisplayName(parameters[i]);
				paramDescriptors[i] = param;
			}
			methodDescriptor = new MethodDescriptor(method, paramDescriptors);
		}

		return methodDescriptor;
	}
}
