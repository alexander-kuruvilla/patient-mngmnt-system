package com.pm.stack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.BootstraplessSynthesizer;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Token;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.CloudMapNamespaceOptions;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.ecs.FargateTaskDefinition;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

public class LocalStack extends Stack {
	
	private final Vpc vpc;
	private final Cluster ecsCluster;
	
	public LocalStack (final App scope, final String id, final StackProps props) {
		super(scope, id, props);
		
		this.vpc = createVpc();
		
		DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
		DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");
		
		
		//calling health checks
		
		CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
		CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");
		
		CfnCluster mskCluster = createMskCluster();
		
		this.ecsCluster = createEcsCluster();
		
		FargateService authService = createFargateService("AuthService",
				"auth-service",
				List.of(4005),
				authServiceDb,
				Map.of("JWT_SECRET", "a751cdb63cd0b9cc723132f6dfe18ee062ab56a5f34c6f17fd974879d324217588673fc4260f409a2d343d6143a0117f71377cd1a1d84edb97fc368078c1cd11a86f503e494dabfff5b96688711a1791106cafb9eeb5a20fd0cf97b38bfee5d34eb9bef4ff5101e199fdd1574918325e5c5b1b9da0f7a4563c48e2576c79f324cf4c5c1e649f3ae342282dabdb0869cd44959cd7ef652ac1285352539de7585246393d66476c1b59ac25bd99b66153acdfc32bc432730a8b6a6142cc2cf0cbc021a69ef7fe87f72d2a09ed866319c5365dc93e56db8329d7bff3d9a5f1a43fa60f42d013d2b1c00931c94e8257d9ca08579081c2be5c906c8f22a37ceff18adf"));
		
		
		// specifies that the authservice has below two dependencies
		authService.getNode().addDependency(authDbHealthCheck);
		authService.getNode().addDependency(authServiceDb);
		
		FargateService billingService = 
				createFargateService("BillingService",
						"billing-service",
						List.of(4001,9001),
						null,
						null);
		
		FargateService analyticsService = 
				createFargateService("Analytics",
						"analytics-service",
						List.of(4002),
						null,
						null);
		analyticsService.getNode().addDependency(mskCluster);
		
		FargateService patientService =
				createFargateService("PatientService",
						"patient-service",
						List.of(4000),
						patientServiceDb,
						Map.of(
								"BILLING_SERVICE_ADDRESS", "host.docker.internal",
								"BILLING_SERVICE_GRPC_PORT", "9001"
								));
		patientService.getNode().addDependency(patientServiceDb);
		patientService.getNode().addDependency(patientDbHealthCheck);
		patientService.getNode().addDependency(billingService);
		patientService.getNode().addDependency(mskCluster);
		
		createApiGatewayService();
		
	}
	
	private Vpc createVpc() {
		return Vpc.Builder.create(this, "patient-management-vpc").vpcName("PatientManagementVPC")
					.vpcName("PatientManagementVPC")
					.maxAzs(2)
					.build();
	}
	
	private DatabaseInstance createDatabase(String id, String dbName) {
		return DatabaseInstance.Builder.create(this, id)
				.engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
						.version(PostgresEngineVersion.VER_17_2)
						.build()))
				.vpc(vpc)
				.instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
				.allocatedStorage(20)
				.credentials(Credentials.fromGeneratedSecret("admin_user"))
				.databaseName(dbName)
				.removalPolicy(RemovalPolicy.DESTROY)
				.build();
	}
	
	private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
		return CfnHealthCheck.Builder.create(this, id)
				.healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
						.type("TCP")
						.port(Token.asNumber(db.getDbInstanceEndpointPort()))
						.ipAddress(db.getDbInstanceEndpointAddress())
						.requestInterval(30)
						.failureThreshold(3)
						.build())
				.build();
	}
	
	private CfnCluster createMskCluster() {
		return CfnCluster.Builder.create(this,"MskCluster")
				.clusterName("kafa-cluster")
				.kafkaVersion("2.8.0")
				.numberOfBrokerNodes(2)
				.brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
						.instanceType("kafka.m5.xlarge")
						.clientSubnets(vpc.getPrivateSubnets().stream()
								.map(ISubnet::getSubnetId)
								.toList())
						.brokerAzDistribution("DEFAULT")
						.build())
				.build();
	}
	
	private Cluster createEcsCluster() {
		return Cluster.Builder.create(this, "PatientManagementCluster")
				.vpc(vpc)
				.defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
						.name("patient-management.local")
						.build())
				.build();
	}
	
	private FargateService createFargateService(String id, String imageName, List<Integer> ports, DatabaseInstance db, Map<String, String> additionalEnvVars) {
		
		FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id+"Task")
				.cpu(256)								
				.memoryLimitMiB(512)
				.build();
		
		ContainerDefinitionOptions.Builder containerOptions = 
				ContainerDefinitionOptions.builder()
				.image(ContainerImage.fromRegistry(imageName))
				.portMappings(ports.stream()
						.map(port -> PortMapping.builder()
								.containerPort(port)
								.hostPort(port)
								.protocol(Protocol.TCP)
						.build())
						.toList())
				.logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
						.logGroup(LogGroup.Builder.create(this, id+"LogGroup")
								.logGroupName("/ecs/" + imageName)
								.removalPolicy(RemovalPolicy.DESTROY)
								.retention(RetentionDays.ONE_DAY)
								.build())
						.streamPrefix(imageName)
						.build()));
		
		Map<String,String> envVars = new HashMap<>();
		envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
		
		if(additionalEnvVars != null) {
			envVars.putAll(additionalEnvVars);
		}
		
		if(db != null) {
			envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
					db.getDbInstanceEndpointAddress(),
					db.getDbInstanceEndpointPort(),
					imageName));
			envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
			envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
			envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
			envVars.put("SPRING_SQL_INIT_MODE", "always");
			envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");

		}
		
		containerOptions.environment(envVars);
		taskDefinition.addContainer(imageName + "Container", containerOptions.build());
		
		return FargateService.Builder.create(this, id)
				.cluster(ecsCluster)
				.taskDefinition(taskDefinition)
				.assignPublicIp(false)
				.serviceName(imageName)
				.build();
	}
	
	//special case service was needed for the api gateway as it need access to the public
	//and the private
	// so instead of modifying the fargate service a new one was created
	
	private void createApiGatewayService() {
		
		FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "APIGatewayTaskDefinition")
				.cpu(256)								
				.memoryLimitMiB(512)
				.build();
		
		ContainerDefinitionOptions containerOptions = 
				ContainerDefinitionOptions.builder()
				.image(ContainerImage.fromRegistry("api-gateway"))
				.environment(Map.of(
						"SPRING_PROFILES_ACTIVE", "prod",
						"AUTH_SERVICE_URL", "http://host.docker.internal:4005"
						))
				.portMappings(List.of(4004).stream()
						.map(port -> PortMapping.builder()
								.containerPort(port)
								.hostPort(port)
								.protocol(Protocol.TCP)
						.build())
						.toList())
				.logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
						.logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
								.logGroupName("/ecs/api-gateway")
								.removalPolicy(RemovalPolicy.DESTROY)
								.retention(RetentionDays.ONE_DAY)
								.build())
						.streamPrefix("api-gateway")
						.build()))
				.build();
		
		
		taskDefinition.addContainer("APIGatewayContainer", containerOptions);
		
		//autocreates a api gateway for us instead of fargate service
		ApplicationLoadBalancedFargateService apiGateway =
				ApplicationLoadBalancedFargateService.Builder.create(this, "APIGatewayService")
				.cluster(ecsCluster)
				.serviceName("api-gateway")
				.taskDefinition(taskDefinition)
				.desiredCount(1)
				.healthCheckGracePeriod(Duration.seconds(60))
				.build();
		
	}
	
	public static void main(final String[] args) {
		App app = new App(AppProps.builder().outdir("./cdk.out").build());
		StackProps props = StackProps.builder()
				.synthesizer(new BootstraplessSynthesizer())
				.build();
		
		new LocalStack(app, "localstack", props);
		app.synth();
		System.out.println("App synthesizing in progress....");
	}

}
