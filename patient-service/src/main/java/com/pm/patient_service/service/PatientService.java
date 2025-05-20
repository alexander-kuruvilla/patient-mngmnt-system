package com.pm.patient_service.service;

import java.util.List;
import java.util.UUID;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;

public interface PatientService {
	
	public List<PatientResponseDTO> getPatients();
	public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO);
	public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO);
	public void deletePatient(UUID id);
	

}
