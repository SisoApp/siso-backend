package com.siso.user.dto.request;

import com.siso.user.domain.model.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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
    private String location;
    private Sex sex;
    private PreferenceSex preferenceSex;
    private Long profileImageId;
    private Mbti mbti;
    
    @Size(min = 3, max = 7, message = "Meeting은 최소 3개 이상, 최대 7개 이하로 선택해야 합니다.")
    private List<Meeting> meetings;
}
