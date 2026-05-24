package com.pkfrc.rdvservice.repository;

import com.pkfrc.rdvservice.enumeration.NomService;
import com.pkfrc.rdvservice.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {
    Optional<Services> findByIdAndIsDeletedFalse(Long id);
    Boolean existsByNomAndIsDeletedFalse(NomService nom);
    List<Services> findAllByIsDeletedFalse();
}
