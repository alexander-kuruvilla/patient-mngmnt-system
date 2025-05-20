package com.pm.auth_service.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pm.auth_service.model.User;
import com.pm.auth_service.repository.UserRepository;
import com.pm.auth_service.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Optional<User> findByEmail(String email) {
		log.info("the service::::"+ email);
		return userRepository.findByEmail(email);
	}

}
