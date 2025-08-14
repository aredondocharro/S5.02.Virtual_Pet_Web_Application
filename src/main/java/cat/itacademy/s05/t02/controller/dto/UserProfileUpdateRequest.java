package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Editable profile fields")
public class UserProfileUpdateRequest {

    @Schema(description = "New name visible (opcional)", example = "AlexGamer")
    private String username;

    @Schema(description = "New bio (optional)", example = "Cat lover. Pixel artist.")
    private String bio;

    @Schema(description = "New URL picture (optional)", example = "https://i.pravatar.cc/200?img=12")
    private String avatarUrl;

    public UserProfileUpdateRequest() {}

    public UserProfileUpdateRequest(String username, String bio, String avatarUrl) {
        this.username = username;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() { return username; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setUsername(String username) { this.username = username; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
