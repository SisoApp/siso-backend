package com.siso.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EditMessageRequestDto {
    private Long messageId;
    private String newContent;

    public EditMessageRequestDto(Long messageId, String newContent) {
        this.messageId = messageId;
        this.newContent = newContent;
    }

    public void assignMessageId(Long messageId) {
        this.messageId = messageId;
    }
}