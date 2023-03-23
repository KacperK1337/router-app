package pl.kacperk.routerapp.router.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.kacperk.routerapp.message.model.Message;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;
import pl.kacperk.routerapp.router.repo.RouterRepo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static pl.kacperk.routerapp.router.model.RouterStatus.DISCONNECTED;
import static pl.kacperk.routerapp.router.model.RouterStatus.LOST;
import static pl.kacperk.routerapp.router.model.RouterStatus.MALFUNCTION;
import static pl.kacperk.routerapp.router.model.RouterStatus.WORKING;
import static pl.kacperk.routerapp.router.util.RouterUtils.findPossibleRouterStatus;

@Service
@RequiredArgsConstructor
public class RouterServiceImpl implements RouterService {
    private final RouterRepo routerRepo;

    @Override
    public void addRouter(String ipAddress) {
        Router router = new Router();
        router.setIpAddress(ipAddress);
        router.setStatus(RouterStatus.WORKING);
        routerRepo.save(router);
    }

    @Override
    public Router getRouterByIP(String ipAddress) {
        return routerRepo.getRouterByIpAddress(ipAddress).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND, String.format("Router with ip %s not found", ipAddress)
                ));
    }

    @Transactional
    @Override
    public void updateRouterStatus(String ipAddress, RouterStatus status) {
        Router router = getRouterByIP(ipAddress);
        router.setStatus(status);
    }

    @Transactional
    @Override
    public void updateRoutersFromMessageMap(
            Map<String, List<Message>> messagesMap,
            int messagesRateSec,
            Set<String> ignoredIps
    ) {
        messagesMap.forEach((ipAddress, messages) -> {
            RouterStatus possibleStatus = findPossibleRouterStatus(messages, messagesRateSec);
            if (possibleStatus.equals(MALFUNCTION)) {
                updateRouterStatus(ipAddress, MALFUNCTION);
                ignoredIps.add(ipAddress);
            } else {
                RouterStatus currentStatus = getRouterByIP(ipAddress).getStatus();
                if (possibleStatus.equals(LOST)) {
                    if (currentStatus.equals(WORKING)) {
                        updateRouterStatus(ipAddress, DISCONNECTED);
                    } else if (currentStatus.equals(DISCONNECTED)) {
                        updateRouterStatus(ipAddress, LOST);
                        ignoredIps.add(ipAddress);
                    }
                } else {
                    if (currentStatus.equals(DISCONNECTED)) {
                        updateRouterStatus(ipAddress, WORKING);
                    }
                }
            }
        });
    }

}
