package com.pkfrc.rdvservice.repository;

import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.enumeration.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class UtilisateurRepositoryTest {

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
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Utilisateur utilisateur1;
    private Utilisateur utilisateur2;

    @BeforeEach
    void setUp() {
        utilisateur1 = Utilisateur.builder()
                .email("john.doe@example.com")
                .username("johndoe")
                .password("password123")
                .telephone("0612345678")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .role(Role.CLIENT)
                .isDeleted(false)
                .build();

        utilisateur2 = Utilisateur.builder()
                .email("jane.smith@example.com")
                .username("janesmith")
                .password("password456")
                .telephone("0698765432")
                .dateNaissance(LocalDate.of(1985, 5, 15))
                .role(Role.RESPONSABLE)
                .isDeleted(false)
                .build();

        entityManager.persist(utilisateur1);
        entityManager.persist(utilisateur2);
        entityManager.flush();
    }

    @Test
    void findByEmailAndIsDeletedFalse_ShouldReturnUser_WhenEmailExists() {
        Optional<Utilisateur> found = utilisateurRepository.findByEmailAndIsDeletedFalse("john.doe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByEmailAndIsDeletedFalse_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<Utilisateur> found = utilisateurRepository.findByEmailAndIsDeletedFalse("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findByEmailAndIsDeletedFalse_ShouldReturnEmpty_WhenUserIsDeleted() {
        utilisateur1.setIsDeleted(true);
        entityManager.persist(utilisateur1);
        entityManager.flush();

        Optional<Utilisateur> found = utilisateurRepository.findByEmailAndIsDeletedFalse("john.doe@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUsernameAndIsDeletedFalse_ShouldReturnUser_WhenUsernameExists() {
        Optional<Utilisateur> found = utilisateurRepository.findByUsernameAndIsDeletedFalse("janesmith");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("janesmith");
    }

    @Test
    void findByUsernameAndIsDeletedFalse_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        Optional<Utilisateur> found = utilisateurRepository.findByUsernameAndIsDeletedFalse("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void findActiveById_ShouldReturnUser_WhenUserExistsAndNotDeleted() {
        Optional<Utilisateur> found = utilisateurRepository.findActiveById(utilisateur1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(utilisateur1.getId());
    }

    @Test
    void findActiveById_ShouldReturnEmpty_WhenUserIsDeleted() {
        utilisateur1.setIsDeleted(true);
        entityManager.persist(utilisateur1);
        entityManager.flush();

        Optional<Utilisateur> found = utilisateurRepository.findActiveById(utilisateur1.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmailAndIsDeletedFalse_ShouldReturnTrue_WhenEmailExists() {
        boolean exists = utilisateurRepository.existsByEmailAndIsDeletedFalse("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailAndIsDeletedFalse_ShouldReturnFalse_WhenEmailDoesNotExist() {
        boolean exists = utilisateurRepository.existsByEmailAndIsDeletedFalse("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsernameAndIsDeletedFalse_ShouldReturnTrue_WhenUsernameExists() {
        boolean exists = utilisateurRepository.existsByUsernameAndIsDeletedFalse("janesmith");

        assertThat(exists).isTrue();
    }

    @Test
    void findAllActive_ShouldReturnOnlyNonDeletedUsers() {
        utilisateur1.setIsDeleted(true);
        entityManager.persist(utilisateur1);
        entityManager.flush();

        List<Utilisateur> activeUsers = utilisateurRepository.findAllActive();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getId()).isEqualTo(utilisateur2.getId());
    }

    @Test
    void findByRoleAndIsDeletedFalse_ShouldReturnUsersByRole() {
        List<Utilisateur> clients = utilisateurRepository.findByRoleAndIsDeletedFalse(Role.CLIENT);
        List<Utilisateur> responsables = utilisateurRepository.findByRoleAndIsDeletedFalse(Role.RESPONSABLE);

        assertThat(clients).hasSize(1);
        assertThat(clients.getFirst().getRole()).isEqualTo(Role.CLIENT);
        assertThat(responsables).hasSize(1);
        assertThat(responsables.getFirst().getRole()).isEqualTo(Role.RESPONSABLE);
    }
}