package com.pm.auth_service.service;

import java.util.Optional;

import com.pm.auth_service.dto.LoginRequestDTO;

public interface AuthService {
	
	public Optional<String> authenticate(LoginRequestDTO loginRequestDTO);
	
	public boolean validateToken(String token);

}
