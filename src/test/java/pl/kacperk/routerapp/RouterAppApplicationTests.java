package pl.kacperk.routerapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.web.servlet.MockMvc;
import pl.kacperk.routerapp.message.dto.MessageRequestDto;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.RouterStatus;
import pl.kacperk.routerapp.router.service.RouterServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;
import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;
import static pl.kacperk.routerapp.testutil.TestUtils.getTestMessage;

// This test checks real life scenario so can take up to 2-3 minutes to be completed.
@SpringBootTest
@AutoConfigureMockMvc
@EnableScheduling
@Slf4j
class RouterAppApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RouterServiceImpl routerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static List<Message> TEST_MESSAGES;

    @BeforeAll
    static void beforeAll() {
        //    IP  |   Expected Behaviour  |
        //    ----------------------------|
        //    a   |       working         |
        //    b   |       working         |
        //    c   |       working         |
        //    d   |       lost            |
        //    e   |       lost            |
        //    f   |       lost            |
        //    g   |       malfunction     |
        //    h   |       malfunction     |
        //    i   |       lost            |
        //    j   |       malfunction     |

        TEST_MESSAGES = new ArrayList<>(List.of(
                getTestMessage("a", 3, AVAILABLE),

                getTestMessage("b", 3, AVAILABLE),
                getTestMessage("b", 13, GONE),
                getTestMessage("b", 51, AVAILABLE),

                getTestMessage("c", 52, GONE),
                getTestMessage("c", 82, AVAILABLE),

                getTestMessage("d", 35, GONE),

                getTestMessage("e", 6, AVAILABLE),
                getTestMessage("e", 30, GONE),

                getTestMessage("f", 5, GONE),
                getTestMessage("f", 69, AVAILABLE),

                getTestMessage("g", 12, GONE),
                getTestMessage("g", 13, AVAILABLE),
                getTestMessage("g", 24, GONE),
                getTestMessage("g", 26, AVAILABLE),
                getTestMessage("g", 29, GONE),
                getTestMessage("g", 33, AVAILABLE),

                getTestMessage("h", 10, AVAILABLE),
                getTestMessage("h", 14, GONE),
                getTestMessage("h", 17, AVAILABLE),
                getTestMessage("h", 22, GONE),
                getTestMessage("h", 29, AVAILABLE),
                getTestMessage("h", 45, GONE),
                getTestMessage("h", 46, AVAILABLE),

                getTestMessage("i", 8, AVAILABLE),
                getTestMessage("i", 14, GONE),
                getTestMessage("i", 20, AVAILABLE),
                getTestMessage("i", 24, GONE),
                getTestMessage("i", 26, AVAILABLE),
                getTestMessage("i", 29, GONE),
                getTestMessage("i", 95, AVAILABLE),

                getTestMessage("j", 51, AVAILABLE),
                getTestMessage("j", 77, GONE),
                getTestMessage("j", 79, AVAILABLE),
                getTestMessage("j", 82, GONE),
                getTestMessage("j", 83, AVAILABLE),
                getTestMessage("j", 84, GONE),
                getTestMessage("j", 90, AVAILABLE)
        ));

        // Messages are sorted by time because timestamp is given on message send
        // Because of that delays cannot be tested here but are tested in MessageUtilsTest.class
        TEST_MESSAGES.sort(comparing(Message::getTimestamp));
    }

    private void sendMessage(Message msg) throws Exception {
        String ipAddress = msg.getIpAddress();
        MessageRequestDto messageRequest = new MessageRequestDto(
                ipAddress,
                msg.getStatus().name()
        );
        mockMvc.perform(post("/api/messages/send")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageRequest)));
    }

    private void sendMessagesWithTimestamps() throws Exception {
        long startTime = System.currentTimeMillis();
        for (Message msg : TEST_MESSAGES) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            while (elapsedTime < msg.getTimestamp()) {
                elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            }
            sendMessage(msg);
        }
    }

    private void checkIfRouterHasSpecifiedStatus(String routerIp, RouterStatus status) {
        assertThat(routerService.getRouterByIP(routerIp).getStatus())
                .isEqualTo(status);
    }

    private void checkAllRoutersStatuses(){
        checkIfRouterHasSpecifiedStatus("a", WORKING);
        checkIfRouterHasSpecifiedStatus("b", WORKING);
        checkIfRouterHasSpecifiedStatus("c", WORKING);
        checkIfRouterHasSpecifiedStatus("d", LOST);
        checkIfRouterHasSpecifiedStatus("e", LOST);
        checkIfRouterHasSpecifiedStatus("f", LOST);
        checkIfRouterHasSpecifiedStatus("g", MALFUNCTION);
        checkIfRouterHasSpecifiedStatus("h", MALFUNCTION);
        checkIfRouterHasSpecifiedStatus("i", LOST);
        checkIfRouterHasSpecifiedStatus("j", MALFUNCTION);
    }

    @Test
    void appTest() throws Exception {
        // Send all messages based on their timestamps
        log.info("Sending messages...");
        sendMessagesWithTimestamps();
        log.info("All messages send.");

        // Wait on second message check that should update router statuses
        log.info("Waiting on scheduled message check...");
        try {
            Thread.sleep(1000 * 30);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("Scheduled message check has happened.");

        // Check if routers have correct statuses
        log.info("Testing routers...");
        checkAllRoutersStatuses();
        log.info("All routers passed the test.");
    }

}
