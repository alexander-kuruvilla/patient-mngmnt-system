package com.pm.api_gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
	
	private final WebClient webClient;
	
	public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder, @Value("${auth.service.url}") String authServiceUrl) {
		
		this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
		
	}

	@Override
	public GatewayFilter apply(Object config) {
		//exchange contains all the data and props from the request.basically a java variable that represents the current request
		return (exchange, chain) -> {
			String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			
			if(token ==null || !token.startsWith("Bearer ")) {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			}
			
			return webClient.get()
					.uri("/validate") // the rest of the endpoint
					.header(HttpHeaders.AUTHORIZATION, token) //taking auth header from initial request
					.retrieve() // to retrieve the response
					.toBodilessEntity() // informs that response has no body
					.then(chain.filter(exchange)); //this tells spring to continue the request if all is well. no

		};
		
	}
	
}
