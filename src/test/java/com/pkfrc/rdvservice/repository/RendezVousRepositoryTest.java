package com.pkfrc.rdvservice.repository;


import com.pkfrc.rdvservice.entity.RendezVous;
import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.enumeration.NomService;
import com.pkfrc.rdvservice.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class RendezVousRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur client;
    private Utilisateur responsable;
    private Services service;
    private RendezVous rendezVous1;


    @BeforeEach
void setUp() {
    // Persister d'abord toutes les entités parentes
    client = entityManager.persistAndFlush(Utilisateur.builder()
            .email("client@example.com")
            .username("client1")
            .password("password")
            .telephone("0612345678")
            .role(Role.CLIENT)
            .isDeleted(false)
            .build());

    responsable = entityManager.persistAndFlush(Utilisateur.builder()
            .email("responsable@example.com")
            .username("responsable1")
            .password("password")
            .telephone("0698765432")
            .role(Role.RESPONSABLE)
            .isDeleted(false)
            .build());

    service = entityManager.persistAndFlush(Services.builder()
            .nom(NomService.RH)
//            .description("Consultation médicale")
//            .dureeMinutes(60)
//            .prix(50.0)
//            .estActif(true)
            .isDeleted(false)
            .build());

    // Maintenant créer les rendez-vous avec les entités persistées
    rendezVous1 = RendezVous.builder()
            .dateRdv(LocalDateTime.now().plusDays(3))
            .motifRdv("Premier RDV")
            .service(service)
            .responsable(responsable)
            .client(client)
            .isDeleted(false)
            .version(0L)
            .build();

        RendezVous rendezVous2 = RendezVous.builder()
                .dateRdv(LocalDateTime.now().plusDays(4))
                .motifRdv("Deuxième RDV")
                .service(service)
                .responsable(responsable)
                .client(client)
                .isDeleted(false)
                .version(0L)
                .build();

    entityManager.persist(rendezVous1);
    entityManager.persist(rendezVous2);
    entityManager.flush();
}

    @Test
    void findActiveAppointmentsByClient_ShouldReturnClientAppointments() {
        List<RendezVous> appointments = rendezVousRepository.findActiveAppointmentsByClient(client.getId());

        assertThat(appointments).hasSize(2);
        assertThat(appointments.getFirst().getClient().getId()).isEqualTo(client.getId());
    }

    @Test
    void findActiveAppointmentsByResponsable_ShouldReturnResponsableAppointments() {
        List<RendezVous> appointments = rendezVousRepository.findByResponsableId(responsable.getId());

        assertThat(appointments).hasSize(2);
        assertThat(appointments.getFirst().getResponsable().getId()).isEqualTo(responsable.getId());
    }

    @Test
    void findByClientId_ShouldReturnClientAppointments() {
        List<RendezVous> appointments = rendezVousRepository.findByClientId(client.getId());

        assertThat(appointments).hasSize(2);
    }

    @Test
    void findActiveById_ShouldReturnAppointment_WhenExists() {
        var found = rendezVousRepository.findActiveById(rendezVous1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(rendezVous1.getId());
    }

    @Test
    void findActiveById_ShouldReturnEmpty_WhenAppointmentIsDeleted() {
        rendezVous1.setIsDeleted(true);
        entityManager.persist(rendezVous1);
        entityManager.flush();

        var found = rendezVousRepository.findActiveById(rendezVous1.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void isTimeSlotTaken_ShouldReturnTrue_WhenSlotIsTaken() {
        boolean isTaken = rendezVousRepository.isTimeSlotTaken(
                service.getId(),
                rendezVous1.getDateRdv(),
                responsable.getId(),
                client.getId()
        );

        assertThat(isTaken).isTrue();
    }

    @Test
    void isTimeSlotTaken_ShouldReturnFalse_WhenSlotIsFree() {
        LocalDateTime freeSlot = LocalDateTime.now().plusDays(5);

        boolean isTaken = rendezVousRepository.isTimeSlotTaken(
                service.getId(),
                freeSlot,
                responsable.getId(),
                client.getId()
        );

        assertThat(isTaken).isFalse();
    }
}