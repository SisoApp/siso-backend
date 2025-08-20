package com.siso.user.dto.request;

import com.siso.user.domain.model.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileRequestDto {
    private DrinkingCapacity drinkingCapacity;
    private Religion religion;
    private boolean smoke;
    private int age;
    private String nickname;
    private String introduce;
    private PreferenceContact preferenceContact;
    private Location location;
    private Sex sex;

    @Builder
    public UserProfileRequestDto(DrinkingCapacity drinkingCapacity, Religion religion, boolean smoke, int age, String nickname, String introduce, PreferenceContact preferenceContact, Location location, Sex sex) {
        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.preferenceContact = preferenceContact;
        this.location = location;
        this.sex = sex;
    }
}
