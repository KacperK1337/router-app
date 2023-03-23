package pl.kacperk.routerapp.message.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.dto.MessageResponseDto;
import pl.kacperk.routerapp.router.service.RouterServiceImpl;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;
import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock
    private RouterServiceImpl routerService;
    private MessageServiceImpl messageService;
    private final String testIp = "testIp";
    private MessageRequestDto messageRequest;

    @BeforeEach
    void setUp() {
        messageService = new MessageServiceImpl(routerService);
    }

    @Test
    void sendMessageIpNotIgnoredResponseCorrect() {
        messageRequest = new MessageRequestDto(
                testIp, AVAILABLE.name()
        );

        MessageResponseDto receivedResponse = messageService.sendMessage(messageRequest);

        assertThat(receivedResponse.getIpAddress())
                .isEqualTo(testIp);
        assertThat(receivedResponse.getTimestamp())
                .isCloseTo(now().getEpochSecond(), offset(5L));
        assertThat(receivedResponse.getStatus())
                .isEqualTo(AVAILABLE.name());
    }

    @Test
    void sendMessageIpNotIgnoredAndNotPresentInMapRouterServiceMethodInvoked() {
        messageRequest = new MessageRequestDto(
                testIp, AVAILABLE.name()
        );

        messageService.sendMessage(messageRequest);

        verify(routerService).addRouter(testIp);
    }

    @Test
    void sendMessageIpNotIgnoredAndPresentInMapRouterServiceMethodNotInvoked() {
        MessageRequestDto messageRequest1 = new MessageRequestDto(
                testIp, AVAILABLE.name()
        );
        MessageRequestDto messageRequest2 = new MessageRequestDto(
                testIp, GONE.name()
        );
        messageService.sendMessage(messageRequest1);
        messageService.sendMessage(messageRequest2);

        verify(routerService, times(1)).addRouter(testIp);
    }

}
