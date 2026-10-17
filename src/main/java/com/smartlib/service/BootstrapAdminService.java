package com.smartlib.service;

import com.smartlib.SmartLibProperties;
import com.smartlib.domain.AppUser;
import com.smartlib.domain.Role;
import com.smartlib.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdminService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminService.class);

    private final SmartLibProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminService(SmartLibProperties properties, UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrap() {
        SmartLibProperties.Bootstrap bootstrap = properties.getBootstrap();
        if (blank(bootstrap.getAdminUsername()) || blank(bootstrap.getAdminPassword())
                || blank(bootstrap.getAdminEmail()) || blank(bootstrap.getAdminFullName())) {
            log.info("Bootstrap admin is disabled; supply all SMARTLIB_BOOTSTRAP_ADMIN_* variables to enable it");
            return;
        }
        if (bootstrap.getAdminPassword().length() < 16) {
            throw new IllegalStateException("SMARTLIB_BOOTSTRAP_ADMIN_PASSWORD must contain at least 16 characters");
        }
        if (userRepository.existsByUsernameIgnoreCase(bootstrap.getAdminUsername())) {
            log.info("Bootstrap admin username already exists; no account was changed");
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(bootstrap.getAdminEmail())) {
            throw new IllegalStateException("SMARTLIB_BOOTSTRAP_ADMIN_EMAIL is already used by another account");
        }
        try {
            userRepository.save(new AppUser(bootstrap.getAdminUsername().trim(),
                    passwordEncoder.encode(bootstrap.getAdminPassword()), bootstrap.getAdminFullName().trim(),
                    bootstrap.getAdminEmail().trim().toLowerCase(), Role.ADMIN));
            log.info("Bootstrap admin account created for the configured username");
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Could not create the bootstrap admin account", exception);
        }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
