package com.hackathon.agriculture_backend.service;

import com.hackathon.agriculture_backend.model.Farmer;
import com.hackathon.agriculture_backend.model.User;
import com.hackathon.agriculture_backend.repository.FarmerRepository;
import com.hackathon.agriculture_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    public User findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Google ID: " + googleId));
    }
    
    @Transactional
    public User save(User user) {
        User savedUser = userRepository.save(user);
        
        // Create a corresponding Farmer entity for new users
        if (savedUser.getId() != null) {
            try {
                // Check if farmer already exists for this user
                Optional<Farmer> existingFarmer = farmerRepository.findByEmail(savedUser.getEmail());
                if (existingFarmer.isEmpty()) {
                    Farmer farmer = new Farmer();
                    farmer.setName(savedUser.getName());
                    farmer.setPhone("+00000000000"); // Default phone, user can update later
                    farmer.setLocationName("Default Location"); // Default location, user can update later
                    farmer.setLatitude(25.2854); // Default to Doha coordinates
                    farmer.setLongitude(51.5310);
                    farmer.setPreferredCrop("General"); // Default crop
                    farmer.setSmsOptIn(false);
                    farmer.setEmail(savedUser.getEmail()); // Link to user email
                    farmerRepository.save(farmer);
                    log.info("Created Farmer entity for user: {}", savedUser.getEmail());
                }
            } catch (Exception e) {
                log.error("Error creating Farmer entity for user {}: {}", savedUser.getEmail(), e.getMessage());
                // Don't fail user creation if farmer creation fails
            }
        }
        
        return savedUser;
    }
    
    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByGoogleIdOptional(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public boolean existsByGoogleId(String googleId) {
        return userRepository.existsByGoogleId(googleId);
    }
    
    public long count() {
        return userRepository.count();
    }
}
