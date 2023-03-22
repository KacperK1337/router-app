package pl.kacperk.routerapp.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kacperk.routerapp.app.MessageHandler;
import pl.kacperk.routerapp.message.dto.MessageDtoMapper;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.dto.MessageResponseDto;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.message.model.MessageStatus;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageHandler messageHandler;

    @Override
    public MessageResponseDto sendMessage(MessageRequestDto requestDto) {
        Message message = new Message();
        message.setIpAddress(
                requestDto.getIpAddress()
        );
        message.setTimestamp(
                Instant.now().getEpochSecond()
        );
        message.setMessageStatus(
                MessageStatus.valueOf(requestDto.getMessageStatus())
        );
        messageHandler.addMessage(message);
        return MessageDtoMapper.messageToMessageResponseDto(message);
    }
}
