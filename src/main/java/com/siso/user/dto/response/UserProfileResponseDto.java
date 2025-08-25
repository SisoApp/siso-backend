package com.siso.user.dto.response;

import com.siso.image.dto.response.ImageResponseDto;
import com.siso.user.domain.model.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponseDto {
    private DrinkingCapacity drinkingCapacity;
    private Religion religion;
    private boolean smoke;
    private int age;
    private String nickname;
    private String introduce;
    private PreferenceContact preferenceContact;
    private Location location;
    private Sex sex;
    private PreferenceSex preferenceSex;
    private ImageResponseDto profileImage; // 선택된 프로필 이미지 (1개)
    private List<ImageResponseDto> profileImages; // 전체 이미지 목록

    public UserProfileResponseDto(DrinkingCapacity drinkingCapacity,
                                  Religion religion, boolean smoke, int age,
                                  String nickname, String introduce,
                                  PreferenceContact preferenceContact, Location location,
                                  Sex sex, PreferenceSex preferenceSex,
                                  ImageResponseDto profileImage,
                                  List<ImageResponseDto> profileImages) {
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
        this.sex = sex;
        this.preferenceSex = preferenceSex;
        this.profileImage = profileImage;
        this.profileImages = profileImages;
    }
}
