package pl.kacperk.routerapp.message.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kacperk.routerapp.message.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;
import static pl.kacperk.routerapp.message.util.MessageUtils.prepareMapCopy;
import static pl.kacperk.routerapp.testutil.TestUtils.getTestMessage;

class MessageUtilsTest {
    private List<Message> ipMessages;
    private List<Long> ipExpectedTimestamps;

    @BeforeEach
    void setUp() {
        //messages not sorted by timestamp
        String testIp = "testIp";
        List<Message> testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE),
                getTestMessage(testIp, 40, AVAILABLE), //max delay (should not be removed)
                getTestMessage(testIp, 71, AVAILABLE), //max delay + 1 (should be removed)
                getTestMessage(testIp, 5, AVAILABLE) //not sorted (should be first)
        ));
        Map<String, List<Message>> testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        Map<String, List<Message>> preparedMapCopy =
                prepareMapCopy(testMap);
        ipMessages = preparedMapCopy.get(testIp);
    }

    @Test
    void prepareMapCopyMaxDelaysRemoved() {
        ipExpectedTimestamps = new ArrayList<>(List.of(
                10L, 40L, 5L
        ));

        assertThat(ipMessages.stream()
                .map(Message::getTimestamp)
                .collect(toList())
        ).hasSameElementsAs(ipExpectedTimestamps);
    }

    @Test
    void prepareMapCopySortedByTimestamp() {
        ipExpectedTimestamps = new ArrayList<>(List.of(
                5L, 10L, 40L
        ));

        assertThat(ipMessages.stream()
                .map(Message::getTimestamp)
                .collect(toList())
        ).isEqualTo(ipExpectedTimestamps);
    }

}
