package com.hackathon.agriculture_backend.service;

import com.hackathon.agriculture_backend.dto.FarmerDto;
import com.hackathon.agriculture_backend.model.Farmer;
import com.hackathon.agriculture_backend.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FarmerService {
    
    private final FarmerRepository farmerRepository;
    
    public FarmerDto createFarmer(FarmerDto farmerDto) {
        log.info("Creating farmer: {}", farmerDto.getName());
        
        // Check if farmer with same phone already exists
        if (farmerRepository.findByPhone(farmerDto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Farmer with phone number " + farmerDto.getPhone() + " already exists");
        }
        
        Farmer farmer = convertToEntity(farmerDto);
        Farmer savedFarmer = farmerRepository.save(farmer);
        log.info("Farmer created successfully with ID: {}", savedFarmer.getId());
        
        return convertToDto(savedFarmer);
    }
    
    public Optional<FarmerDto> getFarmerById(Long id) {
        log.info("Fetching farmer with ID: {}", id);
        return farmerRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<FarmerDto> getFarmerByPhone(String phone) {
        log.info("Fetching farmer with phone: {}", phone);
        return farmerRepository.findByPhone(phone)
                .map(this::convertToDto);
    }
    
    public Optional<FarmerDto> getFarmerByEmail(String email) {
        log.info("Fetching farmer with email: {}", email);
        return farmerRepository.findByEmail(email)
                .map(this::convertToDto);
    }
    
    public FarmerDto getOrCreateFarmerForUser(String email) {
        log.info("Getting or creating farmer for user email: {}", email);
        
        try {
            // First try to find existing farmer by email
            Optional<Farmer> existingFarmer = farmerRepository.findByEmail(email);
            if (existingFarmer.isPresent()) {
                log.info("Found existing farmer for email: {}", email);
                return convertToDto(existingFarmer.get());
            }
        } catch (Exception e) {
            log.warn("Error finding farmer by email (column might not exist): {}", e.getMessage());
        }
        
        // If no farmer exists or email column doesn't exist, create a new one
        log.info("No farmer found for email: {}, creating new farmer", email);
        Farmer farmer = new Farmer();
        farmer.setName("User"); // Default name, user can update later
        farmer.setPhone("+00000000000"); // Default phone, user can update later
        farmer.setEmail(email);
        farmer.setLocationName("Default Location"); // Default location, user can update later
        farmer.setLatitude(25.2854); // Default to Doha coordinates
        farmer.setLongitude(51.5310);
        farmer.setPreferredCrop("General"); // Default crop
        farmer.setSmsOptIn(false);
        
        Farmer savedFarmer = farmerRepository.save(farmer);
        log.info("Created farmer with ID: {} for email: {}", savedFarmer.getId(), email);
        
        return convertToDto(savedFarmer);
    }
    
    public FarmerDto createFarmerForUserWithoutEmail(String email) {
        log.info("Creating farmer for user email (without email field): {}", email);
        
        // Create new farmer without email field to avoid database issues
        Farmer farmer = new Farmer();
        farmer.setName("User"); // Default name, user can update later
        farmer.setPhone("+00000000000"); // Default phone, user can update later
        farmer.setLocationName("Default Location"); // Default location, user can update later
        farmer.setLatitude(25.2854); // Default to Doha coordinates
        farmer.setLongitude(51.5310);
        farmer.setPreferredCrop("General"); // Default crop
        farmer.setSmsOptIn(false);
        // Don't set email field to avoid database constraint issues
        
        try {
            Farmer savedFarmer = farmerRepository.save(farmer);
            log.info("Created farmer with ID: {} for email: {}", savedFarmer.getId(), email);
            return convertToDto(savedFarmer);
        } catch (Exception e) {
            log.error("Error creating farmer for email {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to create farmer: " + e.getMessage(), e);
        }
    }
    
    public List<FarmerDto> getAllFarmers() {
        log.info("Fetching all farmers");
        return farmerRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FarmerDto> getFarmersWithSmsOptIn() {
        log.info("Fetching farmers with SMS opt-in");
        return farmerRepository.findBySmsOptInTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FarmerDto> getFarmersByLocation(String locationName) {
        log.info("Fetching farmers by location: {}", locationName);
        return farmerRepository.findByLocationName(locationName)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<FarmerDto> getFarmersByCrop(String preferredCrop) {
        log.info("Fetching farmers by crop: {}", preferredCrop);
        return farmerRepository.findByPreferredCrop(preferredCrop)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public FarmerDto updateFarmer(Long id, FarmerDto farmerDto) {
        log.info("Updating farmer with ID: {}", id);
        
        Farmer existingFarmer = farmerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Farmer not found with ID: " + id));
        
        // Check if phone number is being changed and if it already exists
        if (!existingFarmer.getPhone().equals(farmerDto.getPhone())) {
            if (farmerRepository.findByPhone(farmerDto.getPhone()).isPresent()) {
                throw new IllegalArgumentException("Farmer with phone number " + farmerDto.getPhone() + " already exists");
            }
        }
        
        updateFarmerFromDto(existingFarmer, farmerDto);
        Farmer updatedFarmer = farmerRepository.save(existingFarmer);
        log.info("Farmer updated successfully with ID: {}", updatedFarmer.getId());
        
        return convertToDto(updatedFarmer);
    }
    
    public void deleteFarmer(Long id) {
        log.info("Deleting farmer with ID: {}", id);
        
        if (!farmerRepository.existsById(id)) {
            throw new IllegalArgumentException("Farmer not found with ID: " + id);
        }
        
        farmerRepository.deleteById(id);
        log.info("Farmer deleted successfully with ID: {}", id);
    }
    
    public Long getTotalFarmersCount() {
        return farmerRepository.count();
    }
    
    public Long getFarmersWithSmsOptInCount() {
        return farmerRepository.countBySmsOptInTrue();
    }
    
    private Farmer convertToEntity(FarmerDto dto) {
        Farmer farmer = new Farmer();
        farmer.setName(dto.getName());
        farmer.setPhone(dto.getPhone());
        farmer.setEmail(dto.getEmail());
        farmer.setLocationName(dto.getLocationName());
        farmer.setLatitude(dto.getLatitude());
        farmer.setLongitude(dto.getLongitude());
        farmer.setPreferredCrop(dto.getPreferredCrop());
        farmer.setSmsOptIn(dto.getSmsOptIn());
        return farmer;
    }
    
    private FarmerDto convertToDto(Farmer farmer) {
        FarmerDto dto = new FarmerDto();
        dto.setId(farmer.getId());
        dto.setName(farmer.getName());
        dto.setPhone(farmer.getPhone());
        dto.setEmail(farmer.getEmail());
        dto.setLocationName(farmer.getLocationName());
        dto.setLatitude(farmer.getLatitude());
        dto.setLongitude(farmer.getLongitude());
        dto.setPreferredCrop(farmer.getPreferredCrop());
        dto.setSmsOptIn(farmer.getSmsOptIn());
        dto.setCreatedAt(farmer.getCreatedAt());
        dto.setUpdatedAt(farmer.getUpdatedAt());
        return dto;
    }
    
    private void updateFarmerFromDto(Farmer farmer, FarmerDto dto) {
        farmer.setName(dto.getName());
        farmer.setPhone(dto.getPhone());
        farmer.setEmail(dto.getEmail());
        farmer.setLocationName(dto.getLocationName());
        farmer.setLatitude(dto.getLatitude());
        farmer.setLongitude(dto.getLongitude());
        farmer.setPreferredCrop(dto.getPreferredCrop());
        farmer.setSmsOptIn(dto.getSmsOptIn());
    }
}



