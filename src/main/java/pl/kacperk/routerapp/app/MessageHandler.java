package pl.kacperk.routerapp.app;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.RouterStatus;
import pl.kacperk.routerapp.router.service.RouterServiceImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static pl.kacperk.routerapp.message.model.MessageStatus.GONE;
import static pl.kacperk.routerapp.router.model.RouterStatus.DISCONNECTED;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class MessageHandler {
    private final RouterServiceImpl routerService;
    private final Map<String, List<Message>> IPMessagesMap = new ConcurrentHashMap<>();
    private final Set<String> ignoredIPs = ConcurrentHashMap.newKeySet();
    private final int messagesRateSec = 60;
    private final int messagesMaxDelaySec = 30;

    public void addMessage(Message msg) {
        String IPAddress = msg.getIPAddress();
        if (!ignoredIPs.contains(IPAddress)) {
            List<Message> messages = IPMessagesMap.get(IPAddress);
            if (messages != null) {
                messages.add(msg);
            } else {
                IPMessagesMap.put(IPAddress, new ArrayList<>(List.of(msg)));
                routerService.addRouter(IPAddress);
            }
        }
    }

    private Map<String, List<Message>> prepareMapCopy() {
        Map<String, List<Message>> IPMessagesMapPortion = new HashMap<>(IPMessagesMap);

        IPMessagesMapPortion.values() //ignore max delays
                .forEach(messages -> {
                    ListIterator<Message> iterator = messages.listIterator();
                    Message previousMsg = iterator.next();
                    while (iterator.hasNext()) {
                        Message currentMsg = iterator.next();
                        if (previousMsg.getTimestamp() - currentMsg.getTimestamp() > messagesMaxDelaySec) {
                            iterator.previous();
                            iterator.previous();
                            iterator.remove();
                        }
                        previousMsg = currentMsg;
                    }
                });
        IPMessagesMapPortion.values() //sort by timestamps
                .forEach(messages -> messages.sort(Comparator.comparing(Message::getTimestamp)));
        return IPMessagesMapPortion;
    }

    private RouterStatus findPossibleRouterStatus(List<Message> messages) {
        int numMessages = messages.size();
        for (int i = 0; i < numMessages; i++) {
            Message firstMsg = messages.get(i);
            if (firstMsg.getMessageStatus().equals(GONE)) {
                long firstMsgTimestamp = firstMsg.getTimestamp();
                if (numMessages - i == 1
                        || messages.get(i + 1).getTimestamp() - firstMsgTimestamp > messagesRateSec) {
                    return LOST;
                }
                if (numMessages - i >= 6) {
                    if (messages.get(i + 5).getTimestamp() - firstMsgTimestamp <= messagesRateSec) {
                        return MALFUNCTION;
                    }
                } else {
                    break;
                }
            }
        }
        return WORKING;
    }

    private void createRouters(Map<String, List<Message>> messagesMap) {
        messagesMap.forEach((IPAddress, messages) -> {
            RouterStatus possibleStatus = findPossibleRouterStatus(messages);
            if (possibleStatus.equals(MALFUNCTION)) {
                routerService.updateRouterStatus(IPAddress, MALFUNCTION);
                ignoredIPs.add(IPAddress);
            } else {
                RouterStatus currentStatus = routerService.getRouterByIP(IPAddress).getRouterStatus();
                if (possibleStatus.equals(LOST)) {
                    if (currentStatus.equals(WORKING)) {
                        routerService.updateRouterStatus(IPAddress, DISCONNECTED);
                    } else if (currentStatus.equals(DISCONNECTED)) {
                        routerService.updateRouterStatus(IPAddress, LOST);
                        ignoredIPs.add(IPAddress);
                    }
                } else {
                    if (currentStatus.equals(DISCONNECTED)) {
                        routerService.updateRouterStatus(IPAddress, WORKING);
                    }
                }
            }
        });
    }

    private void removeResolvedRouters() {
        ignoredIPs.forEach(IPMessagesMap::remove);
    }

    @Scheduled(fixedRate = messagesRateSec * 1000, initialDelay = messagesRateSec * 1000)
    public void checkMessages() {
        Map<String, List<Message>> messagesMapCopy = prepareMapCopy();
        createRouters(messagesMapCopy);
        removeResolvedRouters();
    }

}
