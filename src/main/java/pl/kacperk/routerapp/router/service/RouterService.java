package pl.kacperk.routerapp.router.service;

import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RouterService {

    void addRouter(String ipAddress);

    Router getRouterByIP(String ipAddress);

    void updateRouterStatus(String ipAddress, RouterStatus status);

    void updateRoutersFromMessageMap(
            Map<String, List<Message>> messagesMap,
            int messagesRateSec,
            Set<String> ignoredIps
    );

}
