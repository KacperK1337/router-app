package pl.kacperk.routerapp.router.service;

import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;

public interface RouterService {

    void addRouter(String ipAddress);

    Router getRouterByIP(String ipAddress);

    void updateRouterStatus(String ipAddress, RouterStatus routerStatus);

}
