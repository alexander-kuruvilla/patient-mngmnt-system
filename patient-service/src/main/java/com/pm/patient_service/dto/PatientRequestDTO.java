package com.pm.patient_service.dto;

import com.pm.patient_service.dto.validators.CreatePatientValidationGroup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientRequestDTO {
	
	@NotBlank(message = "Name is required")
	@Size(max = 100, message = "Name cannot exceed 100 characters")
	private String name;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	private String email;
	
	@NotBlank(message = "Address is required")
	private String address;
	
	@NotBlank(message = "Date of birth is required")
	private String dateOfBirth;
	
//	@NotNull(message = "Registered date is required")
	@NotBlank(groups = CreatePatientValidationGroup.class, message = "Registered date is required")
	private String registeredDate;

}
