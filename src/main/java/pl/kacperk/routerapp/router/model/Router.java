package pl.kacperk.routerapp.router.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Router")
@Table(
        name = "routers",
        uniqueConstraints = {
                @UniqueConstraint(name = "router_ip_address_unique", columnNames = "ip_address")
        }
)
public class Router {
    @Id
    @SequenceGenerator(
            name = "router_id_sequence",
            sequenceName = "router_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "router_id_sequence"
    )
    @Column(
            name = "id"
    )
    private Long id;

    @Column(
            name = "ip_address",
            columnDefinition = "varchar(39)",
            nullable = false
    )
    private String ipAddress;

    @Column(
            name = "status",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private RouterStatus status;
}
