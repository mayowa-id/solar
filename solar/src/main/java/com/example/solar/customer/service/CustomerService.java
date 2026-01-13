package com.example.solar.customer.service;

import com.example.solar.common.exception.ResourceNotFoundException;
import com.example.solar.common.exception.ValidationException;
import com.example.solar.common.util.GeoUtils;
import com.example.solar.common.util.ValidationUtils;
import com.example.solar.customer.domain.Customer;
import com.example.solar.customer.dto.CreateCustomerRequest;
import com.example.solar.customer.dto.CustomerDto;
import com.example.solar.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        // Validate email
        if (!ValidationUtils.isValidEmail(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Customer with email " + request.getEmail() + " already exists");
        }

        // Check if userId already exists
        if (request.getUserId() != null && customerRepository.existsByUserId(request.getUserId())) {
            throw new ValidationException("Customer with userId already exists");
        }

        // Validate coordinates if provided
        if (request.getLatitude() != null || request.getLongitude() != null) {
            if (!GeoUtils.isValidLatitude(request.getLatitude())) {
                throw new ValidationException("Invalid latitude value");
            }
            if (!GeoUtils.isValidLongitude(request.getLongitude())) {
                throw new ValidationException("Invalid longitude value");
            }
        }

        // Create customer entity
        Customer customer = Customer.builder()
                .userId(request.getUserId() != null ? request.getUserId() : UUID.randomUUID())
                .name(ValidationUtils.sanitize(request.getName()))
                .email(ValidationUtils.sanitize(request.getEmail().toLowerCase()))
                .phone(ValidationUtils.sanitize(request.getPhone()))
                .address(ValidationUtils.sanitize(request.getAddress()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());

        return mapToDto(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByUserId(UUID userId) {
        log.info("Fetching customer with userId: {}", userId);
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "userId", userId));
        return mapToDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapToDto(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CreateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Update fields
        if (ValidationUtils.isNotEmpty(request.getName())) {
            customer.setName(ValidationUtils.sanitize(request.getName()));
        }
        if (ValidationUtils.isNotEmpty(request.getPhone())) {
            customer.setPhone(ValidationUtils.sanitize(request.getPhone()));
        }
        if (ValidationUtils.isNotEmpty(request.getAddress())) {
            customer.setAddress(ValidationUtils.sanitize(request.getAddress()));
        }
        if (request.getLatitude() != null && request.getLongitude() != null) {
            if (!GeoUtils.isValidLatitude(request.getLatitude())) {
                throw new ValidationException("Invalid latitude value");
            }
            if (!GeoUtils.isValidLongitude(request.getLongitude())) {
                throw new ValidationException("Invalid longitude value");
            }
            customer.setLatitude(request.getLatitude());
            customer.setLongitude(request.getLongitude());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

        return mapToDto(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    private CustomerDto mapToDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .latitude(customer.getLatitude())
                .longitude(customer.getLongitude())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}