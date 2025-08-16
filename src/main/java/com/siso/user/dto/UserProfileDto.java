package com.siso.user.dto;

import com.siso.user.domain.model.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProfileDto {
    private Long id;
    private DrinkingCapacity drinkingCapacity;
    private Religion religion;
    private boolean smoke;
    private int age;
    private String nickname;
    private String introduce;
    private PreferenceContact preferenceContact;
    private List<String> Image;
    private Location location;
    private Sex sex;
    private Long userId;

}
