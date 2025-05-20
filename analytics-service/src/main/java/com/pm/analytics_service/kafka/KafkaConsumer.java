package com.pm.analytics_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import patient.events.PatientEvent;

@Service
public class KafkaConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
	// tppics - the kafka topics it comsumes, groupid - it tells kafka who is consuming the service
	@KafkaListener(topics = "patient", groupId = "analytics-service")
	public void consumeEvent(byte[] event) {
		
		try {
			PatientEvent patientEvent = PatientEvent.parseFrom(event);
			// --- performing business logic related to kafka
			log.info("received patient event: [patientId= {}, patientname={}, patientemail = {}]", patientEvent.getPatientId(),patientEvent.getName(),patientEvent.getEmail());
		} catch (InvalidProtocolBufferException e) {
			log.error("error deserialising event {}".concat(e.getMessage()));
			e.printStackTrace();
		}
		
	}

	
}
