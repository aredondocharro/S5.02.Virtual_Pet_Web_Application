package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.UserMapper;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserProfileService {

    private final UserRepository users;

    public UserProfileService(UserRepository users) {
        this.users = users;
    }

    public UserProfileResponse getMe(String email) {
        log.debug("Fetching profile for user='{}'", email);
        UserEntity u = users.findByEmail(email).orElseThrow();
        log.info("Profile fetched for user='{}'", email);
        return UserMapper.toProfile(u);
    }

    public UserProfileResponse updateMe(String email, UserProfileUpdateRequest req) {
        log.debug("Updating profile for user='{}' (username='{}')", email, req.getUsername());
        UserEntity u = users.findByEmail(email).orElseThrow();

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!req.getUsername().equals(u.getUsername()) && users.existsByUsername(req.getUsername())) {
                log.warn("Username '{}' already exists for user='{}'", req.getUsername(), email);
                throw new IllegalArgumentException("The user name already exist.");
            }
            u.setUsername(req.getUsername());
        }
        if (req.getBio() != null) {
            u.setBio(req.getBio());
        }
        if (req.getAvatarUrl() != null) {
            u.setAvatarUrl(req.getAvatarUrl());
        }

        log.info("Profile updated for user='{}'", email);
        return UserMapper.toProfile(u);
    }
}

