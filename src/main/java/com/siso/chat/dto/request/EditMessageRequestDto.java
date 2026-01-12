package com.siso.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMessageRequestDto {
    private Long messageId;
    private String newContent;
}