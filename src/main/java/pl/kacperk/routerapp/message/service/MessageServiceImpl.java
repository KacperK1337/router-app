package pl.kacperk.routerapp.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.dto.MessageResponseDto;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.message.model.MessageStatus;
import pl.kacperk.routerapp.router.service.RouterServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Instant.now;
import static pl.kacperk.routerapp.message.dto.MessageDtoMapper.messageToMessageResponseDto;
import static pl.kacperk.routerapp.message.util.MessageUtils.prepareMapCopy;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final RouterServiceImpl routerService;
    private final Map<String, List<Message>> ipMessagesMap = new ConcurrentHashMap<>();
    private final Set<String> ignoredIps = ConcurrentHashMap.newKeySet();
    private final int messagesRateSec = 60;

    @Override
    public MessageResponseDto sendMessage(MessageRequestDto requestDto) {
        String ipAddress = requestDto.getIpAddress();
        Message msg = new Message();
        msg.setIpAddress(
                ipAddress
        );
        msg.setTimestamp(
                now().getEpochSecond()
        );
        msg.setStatus(
                MessageStatus.valueOf(requestDto.getStatus())
        );

        if (!ignoredIps.contains(ipAddress)) {
            List<Message> messages = ipMessagesMap.get(ipAddress);
            if (messages != null) {
                messages.add(msg);
            } else {
                ipMessagesMap.put(ipAddress, new ArrayList<>(List.of(msg)));
                routerService.addRouter(ipAddress);
            }
        } else {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Blocked ipAddress."
            );
        }
        return messageToMessageResponseDto(msg);
    }

    @Scheduled(fixedRate = messagesRateSec * 1000, initialDelay = messagesRateSec * 1000)
    public void checkMessages() {
        Map<String, List<Message>> messagesMapCopy =
                new HashMap<>(ipMessagesMap);
        Map<String, List<Message>> preparedMapCopy =
                prepareMapCopy(messagesMapCopy);
        routerService.updateRoutersFromMessageMap(preparedMapCopy, messagesRateSec, ignoredIps);
        ignoredIps.forEach(ipMessagesMap::remove);
    }

}
