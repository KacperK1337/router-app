package pl.kacperk.routerapp.router.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kacperk.routerapp.router.model.Router;

import java.util.Optional;

@Repository
public interface RouterRepo extends JpaRepository<Router, Long> {

    Optional<Router> getRouterByIpAddress(String ipAddress);

}
