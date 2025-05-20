package com.pm.patient_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.dto.validators.CreatePatientValidationGroup;
import com.pm.patient_service.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {
	
	private final PatientService patientService;
	
	@Autowired
	public PatientController(PatientService patientService){
		this.patientService = patientService;	
	}
	
	@GetMapping("/")
	@Operation(summary = "Get Patients")
	public ResponseEntity<List<PatientResponseDTO>> getPatient() {
		List<PatientResponseDTO> patientDetails = patientService.getPatients();
		return ResponseEntity.ok().body(patientDetails);
	}
	
	@PostMapping("/")
	@Operation(summary = "Create a new Patient")
	public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO){
		return ResponseEntity.ok().body(patientService.createPatient(patientRequestDTO));
	}
	
	
	//you can use the @valid annotation instead of the @validation(default.class)
	@PutMapping("/{id}")
	@Operation(summary = "update an existing Patient")
	public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO){
		return ResponseEntity.ok().body(patientService.updatePatient(id, patientRequestDTO));
	}	
	
	@DeleteMapping("/{id}")
	@Operation(summary = "delete a Patient")
	public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
		patientService.deletePatient(id);
		return ResponseEntity.noContent().build();
	}
}
