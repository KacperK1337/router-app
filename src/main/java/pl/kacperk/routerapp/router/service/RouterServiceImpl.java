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
    public void addRouter(String IPAddress) {
        Router router = new Router();
        router.setIPAddress(IPAddress);
        router.setRouterStatus(RouterStatus.WORKING);
        routerRepo.save(router);
        System.out.println("Added new working router with ip: " + IPAddress);
    }

    @Override
    public Router getRouterByIP(String IPAddress) {
        return routerRepo.getRouterByIPAddress(IPAddress);
    }

    @Transactional
    @Override
    public void updateRouterStatus(String IPAddress, RouterStatus routerStatus) {
        Router router = getRouterByIP(IPAddress);
        System.out.println("Changed router with ip: " + IPAddress + " from status "
                + router.getRouterStatus().name() + " to status " + routerStatus.name());
        router.setRouterStatus(routerStatus);
    }
}
