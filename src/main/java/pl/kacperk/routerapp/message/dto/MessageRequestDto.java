package pl.kacperk.routerapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class MessageRequestDto {
    @NotNull
    private String ipAddress;
    @NotNull
    private String messageStatus;

}
