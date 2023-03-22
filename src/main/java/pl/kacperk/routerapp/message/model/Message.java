package pl.kacperk.routerapp.message.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor //to test
public class Message {
    private String ipAddress;
    private long timestamp;
    private MessageStatus messageStatus;

}
