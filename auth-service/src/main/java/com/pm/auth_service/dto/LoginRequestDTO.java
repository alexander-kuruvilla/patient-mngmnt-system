package com.pm.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
	
	@NotBlank(message = "email is required")
	@Email(message = "email should be a valid email address")
	private String email;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "password must be at least 8 characters")
	private String password;
	

}
