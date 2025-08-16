package com.siso.user.dto.request;

import com.siso.image.dto.ImageResponseDto;
import com.siso.user.domain.model.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileRequestDto {
    private Long id;
    private DrinkingCapacity drinkingCapacity;
    private Religion religion;
    private boolean smoke;
    private int age;
    private String nickname;
    private String introduce;
    private PreferenceContact preferenceContact;
    private Location location;
    private Sex sex;
    private Long userId;

    @Builder
    public UserProfileRequestDto(Long id, DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, int age, String nickname, String introduce, PreferenceContact preferenceContact, Location location, Sex sex, Long userId) {
        this.id = id;
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
        this.sex = sex;
        this.userId = userId;
    }
}
