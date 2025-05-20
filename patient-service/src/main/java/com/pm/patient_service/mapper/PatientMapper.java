package com.pm.patient_service.mapper;

import java.time.LocalDate;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.model.Patient;

public class PatientMapper {
	public static PatientResponseDTO toDTO(Patient patient) {
		PatientResponseDTO patientDTO = new PatientResponseDTO();
		patientDTO.setId(patient.getId().toString());
		patientDTO.setName(patient.getName());
		patientDTO.setEmail(patient.getEmail());
		patientDTO.setAddress(patient.getAddress());
		patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());
		return patientDTO;
	}
	
	public static Patient toEntity(PatientRequestDTO patientRequestDTO) {
		Patient patient = new Patient();
		patient.setName(patientRequestDTO.getName());
		patient.setEmail(patientRequestDTO.getEmail());
		patient.setAddress(patientRequestDTO.getAddress());
		patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
		patient.setRegisteredDate(LocalDate.parse(patientRequestDTO.getRegisteredDate()));
		
		return patient;
	}

}
