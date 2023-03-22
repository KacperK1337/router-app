package pl.kacperk.routerapp.message.dto;

import lombok.Builder;

@Builder
public class MessageResponseDto {
    private Long id;
    private String IPAddress;
    private long timestamp;
    private String messageStatus;
}
