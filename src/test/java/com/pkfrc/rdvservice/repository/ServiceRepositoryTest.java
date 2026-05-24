package com.pkfrc.rdvservice.repository;


import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.enumeration.NomService;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ServiceRepositoryTest {

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
    private ServiceRepository serviceRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Services service1;
    private Services service2;

    @BeforeEach
    void setUp() {
        service1 = Services.builder()
                .nom(NomService.RH)
                .isDeleted(false)
                .build();

        service2 = Services.builder()
                .nom(NomService.COMPTABILITE)
                .isDeleted(false)
                .build();

        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);
    }

    @Test
    void findByIdAndIsDeletedFalse_ShouldReturnService_WhenExists() {
        Optional<Services> found = serviceRepository.findByIdAndIsDeletedFalse(service1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(service1.getId());
        assertThat(found.get().getNom()).isEqualTo(NomService.RH);
    }

    @Test
    void findByIdAndIsDeletedFalse_ShouldReturnEmpty_WhenServiceIsDeleted() {
        service1.setIsDeleted(true);
        entityManager.persistAndFlush(service1);

        Optional<Services> found = serviceRepository.findByIdAndIsDeletedFalse(service1.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByIdAndIsDeletedFalse_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<Services> found = serviceRepository.findByIdAndIsDeletedFalse(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByIsDeletedFalse_ShouldReturnOnlyNonDeletedServices() {
        service1.setIsDeleted(true);
        entityManager.persistAndFlush(service1);

        List<Services> services = serviceRepository.findAllByIsDeletedFalse();

        assertThat(services).hasSize(1);
        assertThat(services.get(0).getId()).isEqualTo(service2.getId());
    }

    @Test
    void existsByNomAndIsDeletedFalse_ShouldReturnTrue_WhenNomExists() {
        boolean exists = serviceRepository.existsByNomAndIsDeletedFalse(NomService.RH);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNomAndIsDeletedFalse_ShouldReturnFalse_WhenNomDoesNotExist() {
        boolean exists = serviceRepository.existsByNomAndIsDeletedFalse(NomService.ARCHIVE);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNomAndIsDeletedFalse_ShouldReturnFalse_WhenServiceIsDeleted() {
        service1.setIsDeleted(true);
        entityManager.persistAndFlush(service1);

        boolean exists = serviceRepository.existsByNomAndIsDeletedFalse(service1.getNom());

        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistService_WhenValid() {
        Services newService = Services.builder()
                .nom(NomService.DAF)
                .isDeleted(false)
                .build();

        Services saved = serviceRepository.save(newService);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo(NomService.DAF);
        assertThat(saved.getIsDeleted()).isFalse();
    }

    @Test
    void softDelete_ShouldMarkServiceAsDeleted() {
        service1.setIsDeleted(true);
        serviceRepository.save(service1);

        Optional<Services> found = serviceRepository.findById(service1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getIsDeleted()).isTrue();

        List<Services> activeServices = serviceRepository.findAllByIsDeletedFalse();
        assertThat(activeServices).hasSize(1);
    }
}