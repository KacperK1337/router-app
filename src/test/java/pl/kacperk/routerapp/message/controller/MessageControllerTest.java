package pl.kacperk.routerapp.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.service.RouterServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RouterServiceImpl routerService;
    private MessageRequestDto messageRequest;
    private final String testIp = "testIp";
    private final String requestMappingUrl = "/api/messages";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        messageRequest = new MessageRequestDto(
                testIp, AVAILABLE.name()
        );
    }

    @Test
    void sendMessageRouterWithIpFromMessageAdded() throws Exception {
        mockMvc.perform(post(requestMappingUrl + "/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)));

        Router addedRouter = routerService.getRouterByIP(testIp);
        assertThat(addedRouter).isNotNull();
    }

    @Test
    void sendMessageCorrectStatus() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(requestMappingUrl + "/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)));

        resultActions.andExpect(status().isCreated());
    }

    @Test
    void sendMessageCorrectResponse() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(requestMappingUrl + "/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)));

        String responseAsString = resultActions.andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(responseAsString).contains(testIp);
        assertThat(responseAsString).contains("timestamp");
        assertThat(responseAsString).contains(AVAILABLE.name());
    }

}
