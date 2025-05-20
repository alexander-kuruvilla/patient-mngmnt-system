package com.pm.auth_service.service;

import java.util.Optional;

import com.pm.auth_service.model.User;

public interface UserService {
	
	public Optional<User> findByEmail(String email);

}
