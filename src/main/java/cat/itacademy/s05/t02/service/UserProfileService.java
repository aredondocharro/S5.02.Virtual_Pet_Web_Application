package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.UserMapper;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ConflictException;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        UserEntity u = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        log.info("Profile fetched for user='{}'", email);
        return UserMapper.toProfile(u);
    }

    public UserProfileResponse updateMe(String email, UserProfileUpdateRequest req) {
        log.debug("Updating profile for user='{}' (username='{}')", email, req != null ? req.getUsername() : null);

        if (req == null) {
            throw new BadRequestException("Update payload is required");
        }

        UserEntity u = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        // username (opcional)
        if (req.getUsername() != null) {
            String newUsername = req.getUsername().trim();
            if (!StringUtils.hasText(newUsername)) {
                throw new BadRequestException("Username must not be blank");
            }
            if (!newUsername.equals(u.getUsername()) && users.existsByUsername(newUsername)) {
                log.warn("Username '{}' already exists for user='{}'", newUsername, email);
                throw new ConflictException("Username already in use");
            }
            u.setUsername(newUsername);
        }

        if (req.getBio() != null) {
            u.setBio(req.getBio().trim());
        }

        // avatarUrl (opcional)
        if (req.getAvatarUrl() != null) {
            String newAvatar = req.getAvatarUrl().trim();
            if (newAvatar.isEmpty()) {
                throw new BadRequestException("Avatar URL must not be blank");
            }
            u.setAvatarUrl(newAvatar);
        }

        log.info("Profile updated for user='{}'", email);

        return UserMapper.toProfile(u);
    }
}


