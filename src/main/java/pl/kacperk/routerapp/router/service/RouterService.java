package pl.kacperk.routerapp.router.service;

import pl.kacperk.routerapp.router.model.Router;
import pl.kacperk.routerapp.router.model.RouterStatus;

public interface RouterService {

    void addRouter(String IPAddress);

    Router getRouterByIP(String IPAddress);

    void updateRouterStatus(String IPAddress, RouterStatus routerStatus);

}
