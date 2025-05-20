package com.pm.patient_service.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.exception.EmailAlreadyExistsException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.grpc.BilllingServiceGrpcClient;
import com.pm.patient_service.kafka.KafkaProducer;
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.model.Patient;
import com.pm.patient_service.repository.PatientRepository;
import com.pm.patient_service.service.PatientService;

@Service
public class PatientServiceImpl implements PatientService {
	
	private final PatientRepository patientRepository;
	private final BilllingServiceGrpcClient billlingServiceGrpcClient;
	private final KafkaProducer kafkaProducer;
	
	@Autowired
	public PatientServiceImpl(PatientRepository patientRepository, BilllingServiceGrpcClient billlingServiceGrpcClient, KafkaProducer kafkaProducer) {
		this.patientRepository = patientRepository;
		this.billlingServiceGrpcClient = billlingServiceGrpcClient;
		this.kafkaProducer = kafkaProducer;
	}

	@Override
	public List<PatientResponseDTO> getPatients() {
		List<Patient> patients = patientRepository.findAll();
//		List<PatientResponseDTO> patientResponseDTOs = patients.stream().map(patient -> PatientMapper.toDTO(patient)).collect(Collectors.toList());
//		List<PatientResponseDTO> patientResponseDTOs = patients.stream().map(PatientMapper::toDTO).toList();
		return patients.stream().map(PatientMapper::toDTO).toList();
	}
	
	@Override
	public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
		
		if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
			throw new EmailAlreadyExistsException ("A patient with this email" +patientRequestDTO.getEmail() + "already exists");
		}
		
		Patient newPatient = patientRepository.save(PatientMapper.toEntity(patientRequestDTO));
		billlingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(), newPatient.getEmail());
		kafkaProducer.sendEvent(newPatient);
		return PatientMapper.toDTO(newPatient);
	}

	@Override
	public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
		Patient patient = patientRepository.findById(id).orElseThrow(()-> new PatientNotFoundException("Patient not found with ID: "+id));
		if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
			throw new EmailAlreadyExistsException ("A patient with this email" +patientRequestDTO.getEmail() + "already exists");
		}
		
		patient.setName(patientRequestDTO.getName());
		patient.setAddress(patientRequestDTO.getAddress());
		patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
		patient.setEmail(patientRequestDTO.getEmail());
		
		Patient updatedPatient = patientRepository.save(patient);
		
		return PatientMapper.toDTO(updatedPatient);
		
		
		
		
	}

	@Override
	public void deletePatient(UUID id) {
		patientRepository.deleteById(id);
		
	}

}
