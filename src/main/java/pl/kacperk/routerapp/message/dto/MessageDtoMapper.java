package pl.kacperk.routerapp.message.dto;

import pl.kacperk.routerapp.message.model.Message;

public class MessageDtoMapper {

    public static MessageResponseDto messageToMessageResponseDto(Message message) {
        return MessageResponseDto.builder()
                .ipAddress(message.getIpAddress())
                .timestamp(message.getTimestamp())
                .status(message.getStatus().name())
                .build();
    }

}
