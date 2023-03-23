package pl.kacperk.routerapp.message.dto;

import org.junit.jupiter.api.Test;
import pl.kacperk.routerapp.message.model.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;

class MessageDtoMapperTest {

    @Test
    void messageToMessageResponseDtoCorrectResponse() {
        Message message = new Message();
        message.setIpAddress("testIp");
        message.setTimestamp(1234567890);
        message.setStatus(AVAILABLE);

        MessageResponseDto receivedResponse = MessageDtoMapper.messageToMessageResponseDto(message);

        assertThat(receivedResponse.getIpAddress()).isEqualTo("testIp");
        assertThat(receivedResponse.getTimestamp()).isEqualTo(1234567890);
        assertThat(receivedResponse.getStatus()).isEqualTo(AVAILABLE.name());
    }

}
