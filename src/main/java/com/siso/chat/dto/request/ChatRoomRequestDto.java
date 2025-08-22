package com.siso.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequestDto {

    @NotNull(message = "유저 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "전화 ID는 필수입니다")
    private Long callId;
}
