package com.pm.patient_service.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Service
public class BilllingServiceGrpcClient {
	
	private static final Logger log = LoggerFactory.getLogger(BilllingServiceGrpcClient.class);
	
	private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
	
	//localhost:9001/BillingService/CreatePatientAccount
	// aws.grps:123123/BillingService/CreatePatientAccount
	
	//Basically we can control the server address "localhost" and the port "9001" seperately
	public BilllingServiceGrpcClient (
		@Value("${billing.service.address:localhost}") String serverAddress,
		@Value("${billing.service.grpc.port:9001}") int serverPort
			) {
		log.info("Connecting to billing service at {} :{}", serverAddress, serverPort);
		
		//managed channel for managing http connections using grpc
		ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(serverAddress, serverPort).usePlaintext().build();
		
		blockingStub = BillingServiceGrpc.newBlockingStub(managedChannel);	
	}
	
	public BillingResponse createBillingAccount(String patientId, String name, String email) {
		
		BillingRequest request = BillingRequest.newBuilder().setPatientId(patientId).setName(name).setEmail(email).build();
		
		//the below create is a method from the proto file
		BillingResponse response = blockingStub.createBillingAccount(request);
		log.info("Response from the billing service GRPC: {}", response);
		return response;
	}
	

}
