package pl.kacperk.routerapp.router.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;
import pl.kacperk.routerapp.router.repo.RouterRepo;

@Service
@RequiredArgsConstructor
public class RouterServiceImpl implements RouterService {
    private final RouterRepo routerRepo;

    @Override
    public void addRouter(String ipAddress) {
        Router router = new Router();
        router.setIpAddress(ipAddress);
        router.setRouterStatus(RouterStatus.WORKING);
        routerRepo.save(router);
        System.out.println("Added new working router with ip: " + ipAddress);
    }

    @Override
    public Router getRouterByIP(String ipAddress) {
        return routerRepo.getRouterByIpAddress(ipAddress);
    }

    @Transactional
    @Override
    public void updateRouterStatus(String ipAddress, RouterStatus routerStatus) {
        Router router = getRouterByIP(ipAddress);
        System.out.println("Changed router with ip: " + ipAddress + " from status "
                + router.getRouterStatus().name() + " to status " + routerStatus.name());
        router.setRouterStatus(routerStatus);
    }
}
