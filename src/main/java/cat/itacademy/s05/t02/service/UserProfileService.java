package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.UserMapper;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserProfileService {

    private final UserRepository users;

    public UserProfileService(UserRepository users) {
        this.users = users;
    }

    public UserProfileResponse getMe(String email) {
        UserEntity u = users.findByEmail(email).orElseThrow();
        return UserMapper.toProfile(u);
    }

    public UserProfileResponse updateMe(String email, UserProfileUpdateRequest req) {
        UserEntity u = users.findByEmail(email).orElseThrow();

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!req.getUsername().equals(u.getUsername()) && users.existsByUsername(req.getUsername())) {
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

        return UserMapper.toProfile(u);
    }
}
