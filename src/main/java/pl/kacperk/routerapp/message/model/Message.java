package pl.kacperk.routerapp.message.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {
    private String ipAddress;
    private long timestamp;
    private MessageStatus status;
}
