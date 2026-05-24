package com.pkfrc.rdvservice.repository;

import com.pkfrc.rdvservice.entity.RendezVous;
import com.pkfrc.rdvservice.entity.Role;
import com.pkfrc.rdvservice.entity.Utilisateur;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByIdAndIsDeletedFalse(Long id);
    Boolean existsByUsernameAndIsDeletedFalse(String username);
    Boolean existsByEmailAndIsDeletedFalse(String email);
    Boolean existsByTelephoneAndIsDeletedFalse(String Telephone);
    // Méthode 1: Avec @Query explicite
    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.isDeleted = false")
    List<Utilisateur> findByRoleAndIsDeletedFalse(@Param("role") Role role);

    // Méthode 2: Avec nommage conventionnel (Spring Data JPA va l'interpréter automatiquement)
    // List<Utilisateur> findByRoleAndIsDeletedFalse(Role role);

    @Query("SELECT u FROM Utilisateur u WHERE u.isDeleted = false")
    List<Utilisateur> findAllIsDeletedFalse();
    // Recherche active par ID avec Query explicite
    @Query("SELECT u FROM Utilisateur u WHERE u.id = :id AND u.isDeleted = false")
    Optional<Utilisateur> findActiveById(@Param("id") Long id);

    // Récupérer tous les utilisateurs actifs
    @Query("SELECT u FROM Utilisateur u WHERE u.isDeleted = false")
    List<Utilisateur> findAllActive();

    // Méthode 1: Avec @Query explicite
//    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.isDeleted = false")
//    List<Utilisateur> findByRoleAndIsDeletedFalse(@Param("role") Role role);

    // Méthode 2: Avec nommage conventionnel (Spring Data JPA va l'interpréter automatiquement)
    // List<Utilisateur> findByRoleAndIsDeletedFalse(Role role);

    // Recherche par rôle en ignorant la casse
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.role) = LOWER(:role) AND u.isDeleted = false")
    List<Utilisateur> findByRoleIgnoreCaseAndIsDeletedFalse(@Param("role") Role role);

    // Compter les utilisateurs par rôle
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.role = :role AND u.isDeleted = false")
    long countByRoleAndIsDeletedFalse(@Param("role") Role role);

    // Recherche par rôle avec ordre par nom
    @Query("SELECT u FROM Utilisateur u WHERE u.role = :role AND u.isDeleted = false ORDER BY u.username")
    List<Utilisateur> findByRoleAndIsDeletedFalseOrderByNomAsc(@Param("role") Role role);

    // Recherche par multiples rôles
    @Query("SELECT u FROM Utilisateur u WHERE u.role IN (:roles) AND u.isDeleted = false")
    List<Utilisateur> findByRolesInAndIsDeletedFalse(@Param("roles") List<String> roles);

    Optional<Utilisateur> findByUsernameAndIsDeletedFalse(String username);

    Optional<Utilisateur> findByEmailAndIsDeletedFalse(String email);
}
