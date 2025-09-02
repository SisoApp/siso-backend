package com.siso.call.dto.response;

import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileDto {
    private Long id;
    private String nickname;
    private int age;
    private String location;
    private List<String> interests;
    private String profileImageUrl;

    public UserProfileDto(Long id, String nickname, int age, String location, List<String> interests, String profileImageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.age = age;
        this.location = location;
        this.interests = interests;
        this.profileImageUrl = profileImageUrl;
    }

    public static UserProfileDto from(User user) {
        UserProfile profile = user.getUserProfile();
        return new UserProfileDto(
                user.getId(),
                profile.getNickname(),
                profile.getAge(),
                profile.getLocation(),
                user.getUserInterests().stream()
                        .map(userInterest -> userInterest.getInterest().name()) // Enum → String
                        .toList(),
                user.getImages().isEmpty() ? null : user.getImages().get(0).getPath()
        );
    }
}

