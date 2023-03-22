package pl.kacperk.routerapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kacperk.routerapp.message.model.MessageStatus;

@AllArgsConstructor
@Getter
public class MessageRequestDto {
    private String IPAddress;
    private MessageStatus messageStatus;

}
