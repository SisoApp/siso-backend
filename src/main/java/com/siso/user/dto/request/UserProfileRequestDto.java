package com.siso.user.dto.request;

import com.siso.user.domain.model.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileRequestDto {
    private DrinkingCapacity drinkingCapacity;
    private Religion religion;
    private boolean smoke;

    @Min(value = 19, message = "나이는 최소 19세 이상이어야 합니다.")
    @Max(value = 100, message = "나이는 최대 100세 이하이어야 합니다.")
    private int age;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하이어야 합니다.")
    private String nickname;

    @Size(max = 500, message = "자기소개는 최대 500자까지 입력 가능합니다.")
    private String introduce;

    @NotBlank(message = "위치 정보는 필수입니다.")
    private String location;

    @NotNull(message = "성별은 필수입니다.")
    private Sex sex;

    private PreferenceSex preferenceSex;
    private Mbti mbti;

    @NotNull(message = "Meeting은 필수입니다.")
    @Size(min = 3, max = 7, message = "Meeting은 최소 3개 이상, 최대 7개 이하로 선택해야 합니다.")
    private List<Meeting> meetings;
}
