package com.siso.chat.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EditMessageRequestDto {
    private Long messageId;
//    private Long senderId;
    private String newContent;
}