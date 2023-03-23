package pl.kacperk.routerapp.router.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.repo.RouterRepo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static pl.kacperk.routerapp.message.model.MessageStatus.AVAILABLE;
import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;
import static pl.kacperk.routerapp.router.model.RouterStatus.DISCONNECTED;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;
import static pl.kacperk.routerapp.testutil.TestUtils.getTestMessage;
import static pl.kacperk.routerapp.testutil.TestUtils.getTestRouter;

@ExtendWith(MockitoExtension.class)
class RouterServiceImplTest {
    @Mock
    private RouterRepo routerRepo;
    private RouterServiceImpl routerService;
    private final String testIp = "testIp";
    private List<Message> testMessages;
    Map<String, List<Message>> testMap;
    private final int messagesRateSec = 60;
    private Set<String> ignoredIps;

    @BeforeEach
    void setUp() {
        routerService = new RouterServiceImpl(routerRepo);
    }

    @Test
    void addRouterRouterSavedIntoRepo() {
        routerService.addRouter(testIp);

        ArgumentCaptor<Router> routerArgumentCaptor = ArgumentCaptor.forClass(Router.class);
        verify(routerRepo).save(routerArgumentCaptor.capture());
        Router capturedRouter = routerArgumentCaptor.getValue();

        assertThat(capturedRouter.getIpAddress()).isEqualTo(testIp);
        assertThat(capturedRouter.getStatus()).isEqualTo(WORKING);
    }

    @Test
    void getRouterByIPExistingRouterRepoMethodInvoked() {
        Router expectedRouter = getTestRouter(1L, testIp, WORKING);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(expectedRouter));

        routerService.getRouterByIP(testIp);

        verify(routerRepo).getRouterByIpAddress(testIp);
    }

    @Test
    void getRouterByIPNonExistingRouterThrowsResponseStatusException() {
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> routerService.getRouterByIP(testIp))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining(String.format("Router with ip %s not found", testIp));
    }

    @Test
    void updateRouterStatusRouterStatusChanged() {
        Router testRouter = getTestRouter(1L, testIp, WORKING);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));

        routerService.updateRouterStatus(testIp, DISCONNECTED);

        assertThat(testRouter.getStatus()).isEqualTo(DISCONNECTED);
    }

    @Test
    void updateRoutersFromMessageMapWorkingToLostShouldHaveDisconnected() {
        Router testRouter = getTestRouter(1L, testIp, WORKING);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(testRouter.getStatus()).isEqualTo(DISCONNECTED);
    }

    @Test
    void updateRoutersFromMessageMapDisconnectedToLostShouldHaveLost() {
        Router testRouter = getTestRouter(1L, testIp, DISCONNECTED);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(testRouter.getStatus()).isEqualTo(LOST);
    }

    @Test
    void updateRoutersFromMessageMapDisconnectedToLostShouldBeIgnored() {
        Router testRouter = getTestRouter(1L, testIp, DISCONNECTED);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(ignoredIps.contains(testRouter.getIpAddress())).isTrue();
    }

    @Test
    void updateRoutersFromMessageMapDisconnectedToWorkingShouldHaveWorking() {
        Router testRouter = getTestRouter(1L, testIp, DISCONNECTED);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, AVAILABLE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(testRouter.getStatus()).isEqualTo(WORKING);
    }

    @Test
    void updateRoutersFromMessageMapWorkingToMalfunctionShouldHaveMalfunction() {
        Router testRouter = getTestRouter(1L, testIp, WORKING);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 60, AVAILABLE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(testRouter.getStatus()).isEqualTo(MALFUNCTION);
    }

    @Test
    void updateRoutersFromMessageMapWorkingToMalfunctionShouldBeIgnored() {
        Router testRouter = getTestRouter(1L, testIp, WORKING);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 60, AVAILABLE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(ignoredIps.contains(testRouter.getIpAddress())).isTrue();
    }

    @Test
    void updateRoutersFromMessageMapDisconnectedToMalfunctionShouldHaveMalfunction() {
        Router testRouter = getTestRouter(1L, testIp, DISCONNECTED);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 60, AVAILABLE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(testRouter.getStatus()).isEqualTo(MALFUNCTION);
    }

    @Test
    void updateRoutersFromMessageMapDisconnectedToMalfunctionShouldBeIgnored() {
        Router testRouter = getTestRouter(1L, testIp, DISCONNECTED);
        given(routerRepo.getRouterByIpAddress(testIp))
                .willReturn(Optional.of(testRouter));
        testMessages = new ArrayList<>(List.of(
                getTestMessage(testIp, 10, GONE),
                getTestMessage(testIp, 20, AVAILABLE),
                getTestMessage(testIp, 30, GONE),
                getTestMessage(testIp, 40, AVAILABLE),
                getTestMessage(testIp, 50, GONE),
                getTestMessage(testIp, 60, AVAILABLE)
        ));
        testMap = testMessages.stream()
                .collect(groupingBy(Message::getIpAddress));
        ignoredIps = new HashSet<>();

        routerService.updateRoutersFromMessageMap(testMap, messagesRateSec, ignoredIps);

        assertThat(ignoredIps.contains(testRouter.getIpAddress())).isTrue();
    }

}
