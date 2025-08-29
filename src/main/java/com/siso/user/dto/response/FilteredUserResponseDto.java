package com.siso.user.dto.response;

import com.siso.image.dto.response.ImageResponseDto;
import com.siso.user.domain.model.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilteredUserResponseDto {
    private Long userId;
    private Long profileId;
    private String nickname;
    private String introduce;
    private int age;
    private Sex sex;
    private String location;
    private Religion religion;
    private boolean smoke;
    private DrinkingCapacity drinkingCapacity;
    private List<UserInterestResponseDto> interests;
    private List<ImageResponseDto> profileImages;
    private int commonInterestsCount; // 공통 관심사 개수

    public FilteredUserResponseDto(Long userId, Long profileId, String nickname, String introduce,
                                   int age, Sex sex, String location, Religion religion,
                                   boolean smoke, DrinkingCapacity drinkingCapacity,
                                   List<UserInterestResponseDto> interests,
                                   List<ImageResponseDto> profileImages,
                                   int commonInterestsCount) {
        this.userId = userId;
        this.profileId = profileId;
        this.nickname = nickname;
        this.introduce = introduce;
        this.age = age;
        this.sex = sex;
        this.location = location;
        this.religion = religion;
        this.smoke = smoke;
        this.drinkingCapacity = drinkingCapacity;
        this.interests = interests;
        this.profileImages = profileImages;
        this.commonInterestsCount = commonInterestsCount;
    }
}
