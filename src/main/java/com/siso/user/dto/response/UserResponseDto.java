package com.siso.user.dto.response;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {
    private Long id;
    private Provider provider;
    private String email;
    private String phoneNumber;
    private boolean isDeleted;
    private boolean isBlock;

    public UserResponseDto(Long id, Provider provider, String email, String phoneNumber, boolean isDeleted, boolean isBlock) {
        this.id = id;
        this.provider = provider;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isDeleted = isDeleted;
        this.isBlock = isBlock;
    }

    public static Object from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getProvider(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isDeleted(),
                user.isBlock()
        );
    }
}