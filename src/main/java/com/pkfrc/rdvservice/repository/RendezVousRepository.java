package com.pkfrc.rdvservice.repository;

import com.pkfrc.rdvservice.entity.RendezVous;
import com.pkfrc.rdvservice.enumeration.StatutRDV;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    @Query("SELECT r FROM RendezVous r WHERE r.service.id = :serviceId AND r.dateRdv = :dateTime AND r.responsable.id = :responsableId AND r.client.id = :clientId AND r.isDeleted = false")
    Optional<RendezVous> findByServiceIdAndDateRdvWithLockAndClientIdAndResponsableId(
            @Param("serviceId") Long serviceId,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("responsableId") Long responsableId,
            @Param("clientId") Long clientId
    );

    @Query("SELECT COUNT(r) > 0 FROM RendezVous r WHERE r.service.id = :serviceId AND r.dateRdv = :dateTime AND r.responsable.id = :responsableId AND r.client.id = :clientId AND r.isDeleted = false")
    boolean isTimeSlotTaken(
            @Param("serviceId") Long serviceId,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("responsableId") Long responsableId,
            @Param("clientId") Long clientId);

    @Query("SELECT r FROM RendezVous r WHERE r.responsable.id = :responsableId AND r.isDeleted = false")
    List<RendezVous> findByResponsableId(@Param("responsableId") Long responsableId);

    @Query("SELECT r FROM RendezVous r WHERE r.client.id = :clientId AND r.isDeleted = false")
    List<RendezVous> findByClientId(@Param("clientId") Long clientId);

    @Query("SELECT r FROM RendezVous r WHERE r.id = :id AND r.isDeleted = false")
    Optional<RendezVous> findActiveById(@Param("id") Long id);

    @Query("SELECT r FROM RendezVous r WHERE r.dateRdv BETWEEN :start AND :end AND r.isDeleted = false")
    List<RendezVous> findBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("SELECT r FROM RendezVous r WHERE r.id = :id AND r.isDeleted = false")
    Optional<RendezVous> findByIdWithLock(@Param("id") Long id);

    /**
     * Recherche les rendez-vous par service
     */
    @Query("SELECT r FROM RendezVous r WHERE r.service.id = :serviceId AND r.isDeleted = false")
    List<RendezVous> findByServiceId(@Param("serviceId") Long serviceId);

    // Récupérer tous les rendez-vous d'un client (non annulés et non supprimés)
    @Query("SELECT r FROM RendezVous r WHERE r.client.id = :clientId AND r.isDeleted = false")
    List<RendezVous> findActiveAppointmentsByClient(@Param("clientId") Long clientId);

    /**
     * Recherche les rendez-vous par statut
     */
    @Query("SELECT r FROM RendezVous r WHERE r.statut = :statut AND r.isDeleted = false")
    List<RendezVous> findByStatut(@Param("statut") StatutRDV statut);

    /**
     * Recherche les rendez-vous d'un client par statut
     */
    @Query("SELECT r FROM RendezVous r WHERE r.client.id = :clientId AND r.statut = :statut AND r.isDeleted = false")
    List<RendezVous> findByClientIdAndStatut(@Param("clientId") Long clientId, @Param("statut") StatutRDV statut);
}
