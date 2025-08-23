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
    private List<ImageResponseDto> profileImages;

    public UserProfileResponseDto(DrinkingCapacity drinkingCapacity,
                                  Religion religion, boolean smoke, int age,
                                  String nickname, String introduce,
                                  PreferenceContact preferenceContact, Location location,
                                  Sex sex, PreferenceSex preferenceSex,
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
        this.profileImages = profileImages;
    }
}
