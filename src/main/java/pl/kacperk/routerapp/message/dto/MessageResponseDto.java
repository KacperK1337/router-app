package pl.kacperk.routerapp.message.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageResponseDto {
    private String ipAddress;
    private long timestamp;
    private String messageStatus;
}
