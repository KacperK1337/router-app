package pl.kacperk.routerapp.message.service;

import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.dto.MessageResponseDto;

public interface MessageService {

    MessageResponseDto sendMessage(MessageRequestDto requestDto);

}
