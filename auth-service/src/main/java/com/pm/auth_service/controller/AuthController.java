package com.pm.auth_service.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.pm.auth_service.dto.LoginRequestDTO;
import com.pm.auth_service.dto.LoginResponseDTO;
import com.pm.auth_service.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
public class AuthController {
	
	private final AuthService authService;
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	
	public AuthController(AuthService authService) {
		this.authService = authService;
		
	}

	@Operation(summary = "Generate token on user login")
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login (@Valid @RequestBody LoginRequestDTO loginRequestDTO){
		
		log.info("@@@@---@@@_-@@_@_@_@____@dafsdfsdf test test asd as");
		Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);
		
		
		if(tokenOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		String token = tokenOptional.get();
		return ResponseEntity.ok(new LoginResponseDTO(token));
	}
	
	@Operation(summary = "validate token")
	@GetMapping("/validate")
	public ResponseEntity<Void> validateToken(
			@RequestHeader("Authorization") String authHeader){
		
		// Authorization: Bearer <token>
		if(authHeader ==null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		return authService.validateToken(authHeader.substring(7))
				? ResponseEntity.ok().build()
				: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		
		
		
	}
}
