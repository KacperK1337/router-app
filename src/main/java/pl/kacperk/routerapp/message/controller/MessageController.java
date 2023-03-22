package pl.kacperk.routerapp.message.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kacperk.routerapp.message.service.MessageServiceImpl;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.dto.MessageResponseDto;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageServiceImpl messageService;

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody final MessageRequestDto requestDto) {
        MessageResponseDto responseDto = messageService.sendMessage(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
