package com.pm.billingservice.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
	
	private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
	
	@Override
	public void createBillingAccount(billing.BillingRequest billingRequest,
			StreamObserver<BillingResponse> responseObserver) {
		//	stream observer is used to observe multiple responses in real time
		
		log.info("createBillingAccount request received {}", billingRequest.toString());
		
		//business logic
		
		BillingResponse response = BillingResponse.newBuilder()
				.setAccountId("12345")
				.setStatus("ACTIVE")
				.build();
		
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		
		//you can sent several response usign on Next before completing
				
		
	}

}
