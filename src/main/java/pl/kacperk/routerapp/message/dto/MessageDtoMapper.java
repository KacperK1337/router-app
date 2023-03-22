package pl.kacperk.routerapp.message.dto;

import lombok.NonNull;
import pl.kacperk.routerapp.message.model.Message;

public class MessageDtoMapper {

    public static MessageResponseDto messageToMessageResponseDto(@NonNull Message message) {
        return MessageResponseDto.builder()
                .ipAddress(message.getIpAddress())
                .timestamp(message.getTimestamp())
                .messageStatus(message.getMessageStatus().name())
                .build();
    }
}
