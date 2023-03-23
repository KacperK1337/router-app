package pl.kacperk.routerapp.router.util;

import org.junit.jupiter.api.Test;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.RouterStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;
import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;
import static pl.kacperk.routerapp.router.util.RouterUtils.findPossibleRouterStatus;
import static pl.kacperk.routerapp.testutil.TestUtils.getTestMessage;

class RouterUtilsTest {
    private List<Message> testMessages;
    private final String testIp = "testIp";

    @Test
    void findPossibleRouterStatusWorkingWithAvailableOnly() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(WORKING);
    }

    @Test
    void findPossibleRouterStatusWorkingWithGoneAndAvailableWithinRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 70, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(WORKING);
    }

    @Test
    void findPossibleRouterStatusWorkingWithAvailableAndGoneAndAvailableWithinRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE),
                getTestMessage(testIp, 20, GONE),
                getTestMessage(testIp, 80, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(WORKING);
    }

    @Test
    void findPossibleRouterStatusWorkingWith3GoneAvailablePairsOffRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 71, GONE),
                getTestMessage(testIp, 72, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(WORKING);
    }

    @Test
    void findPossibleRouterStatusLostWithGoneOnly() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(LOST);
    }

    @Test
    void findPossibleRouterStatusLostWithAvailableAndGoneOnly() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE),
                getTestMessage(testIp, 20, GONE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(LOST);
    }

    @Test
    void findPossibleRouterStatusLostWithGoneAndAvailableOffRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 71, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(LOST);
    }

    @Test
    void findPossibleRouterStatusLostWithAvailableAndGoneAndAvailableOffRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE),
                getTestMessage(testIp, 20, GONE),
                getTestMessage(testIp, 81, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(LOST);
    }

    @Test
    void findPossibleRouterStatusMalfunctionWith3GoneAvailablePairsWithinRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 70, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(MALFUNCTION);
    }

    @Test
    void findPossibleRouterStatusMalfunctionWithAvailableAnd3GoneAvailablePairsWithinRate() {
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 5, AVAILABLE),
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 70, AVAILABLE)
        ));

        RouterStatus statusReceived = findPossibleRouterStatus(testMessages, 60);

        assertThat(statusReceived).isEqualTo(MALFUNCTION);
    }

}
