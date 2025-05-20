package com.pm.auth_service.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pm.auth_service.dto.LoginRequestDTO;
import com.pm.auth_service.model.User;
import com.pm.auth_service.service.AuthService;
import com.pm.auth_service.service.UserService;
import com.pm.auth_service.util.JwtUtil;

import io.jsonwebtoken.JwtException;

@Service
public class AuthServiceImpl implements AuthService {
	
	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
	
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	
	public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
		
		log.info("login request DTO "+ loginRequestDTO.getEmail());
		log.info("login request DTO "+ loginRequestDTO.getPassword());
		Optional<String> token = userService
				.findByEmail(loginRequestDTO.getEmail())
				.filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
				.map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));
		
		return token;
	}

	@Override
	public boolean validateToken(String token) {
		try {
			jwtUtil.validateToken(token);
			return true;
			
		} catch (JwtException e) {
			return false;
		}
	}

}
