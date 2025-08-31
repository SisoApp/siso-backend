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
    private String location;
    private Sex sex;
    private PreferenceSex preferenceSex;
    private Mbti mbti;
    private List<Meeting> meetings;

    public UserProfileResponseDto(DrinkingCapacity drinkingCapacity,
                                  Religion religion, boolean smoke, int age,
                                  String nickname, String introduce,
                                  String location,
                                  Sex sex, PreferenceSex preferenceSex,
                                  Mbti mbti, List<Meeting> meetings) {

        this.drinkingCapacity = drinkingCapacity;
        this.religion = religion;
        this.smoke = smoke;
        this.age = age;
        this.nickname = nickname;
        this.introduce = introduce;
        this.location = location;
        this.sex = sex;
        this.preferenceSex = preferenceSex;
        this.mbti = mbti;
        this.meetings = meetings;
    }
}
