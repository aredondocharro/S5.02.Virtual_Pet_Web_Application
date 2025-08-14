package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Authenticated user profile response")
public class UserProfileResponse {

    @Schema(example = "7")
    private Long id;

    @Schema(example = "alex@example.com")
    private String email;

    @Schema(description = "Visible name", example = "Alex")
    private String username;

    @Schema(description = "URL profile picture", example = "https://i.pravatar.cc/200?img=5")
    private String avatarUrl;

    @Schema(description = "Short bio", example = "Cat lover. Pixel artist.")
    private String bio;

    @Schema(description = "User role", example = "[\"USER\",\"ADMIN\"]")
    private List<String> roles;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String email, String username,
                               String avatarUrl, String bio, List<String> roles) {
        this.id = id; this.email = email; this.username = username;
        this.avatarUrl = avatarUrl; this.bio = bio; this.roles = roles;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBio() { return bio; }
    public List<String> getRoles() { return roles; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
