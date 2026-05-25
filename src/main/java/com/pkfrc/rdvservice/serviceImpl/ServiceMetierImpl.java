package com.pkfrc.rdvservice.serviceImpl;


import com.pkfrc.rdvservice.dto.ServiceRequest;
import com.pkfrc.rdvservice.dto.ServiceResponse;
import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.repository.ServiceRepository;
import com.pkfrc.rdvservice.serviceFace.ServiceMetierFace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceMetierImpl implements ServiceMetierFace {

    @Autowired
    ServiceRepository serviceRepository;

    /**
     * Créer un nouveau service
     */
    @Transactional(readOnly = true)
    @Override
    public ServiceResponse creerService(ServiceRequest request) {
        log.debug("Création du service : {}", request.nom());

        // Vérifier si le nom existe déjà
        if (serviceRepository.existsByNomAndIsDeletedFalse(request.nom())) {
            throw new BusinessException("SERVICE_CODE_EXISTS",
                    "Un service avec le nom " + request.nom() + " existe déjà");
        }

        Services service = Services.builder()
                .nom(request.nom())
//                .description(request.description())
//                .dureeMinutes(request.dureeMinutes())
//                .prix(request.prix())
//                .estActif(request.estActif() != null ? request.estActif() : true)
                .isDeleted(false)
                .build();

        var savedService = serviceRepository.save(service);
        log.info("Service créé avec succès - ID: {}, Code: {}", savedService.getId(), savedService.getId());

        return mapToResponse(savedService);
    }

    /**
     * Récupérer un service par son ID
     */

    @Transactional(readOnly = true)
    @Override
    public ServiceResponse getServiceById(Long id) {
        log.debug("Recherche du service avec ID: {}", id);

        var service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("SERVICE_NOT_FOUND",
                        "Service non trouvé avec l'ID: " + id));

        return mapToResponse(service);
    }


    /**
     * Récupérer tous les services actifs
     */

    @Transactional(readOnly = true)
    @Override
    public List<ServiceResponse> getAllServicesActifs() {
        log.debug("Récupération de tous les services actifs");

        List<Services> services = serviceRepository.findAllByIsDeletedFalse();
        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les services (y compris inactifs, mais non supprimés)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ServiceResponse> getAllServices() {
        log.debug("Récupération de tous les services");

        var services = serviceRepository.findAll().stream()
                .filter(service -> !service.getIsDeleted())
                .toList();

        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour un service
     */
    @Transactional(readOnly = true)
    @Override
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        log.debug("Mise à jour du service avec ID: {}", id);

        Services service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("SERVICE_NOT_FOUND",
                        "Service non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau code n'est pas déjà utilisé par un autre service
        if (!service.getNom().equals(request.nom()) &&
                serviceRepository.existsByNomAndIsDeletedFalse(request.nom())) {
            throw new BusinessException("SERVICE_NOM_EXISTS",
                    "Un service avec le nom " + request.nom() + " existe déjà");
        }

//        service.setCode(request.code());
        service.setNom(request.nom());
//        service.setDescription(request.description());
//        service.setDureeMinutes(request.dureeMinutes());
//        service.setPrix(request.prix());
//        if (request.estActif() != null) {
//            service.setEstActif(request.estActif());
//        }

        Services updatedService = serviceRepository.save(service);
        log.info("Service mis à jour avec succès - ID: {}", updatedService.getId());

        return mapToResponse(updatedService);
    }

    /**
     * Supprimer un service (soft delete)
     */
    @Transactional(readOnly = true)
    @Override
    public void supprimerService(Long id) {
        log.debug("Suppression soft du service ID: {}", id);

        var service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("SERVICE_NOT_FOUND",
                        "Service non trouvé avec l'ID: " + id));

        service.setIsDeleted(true);
        serviceRepository.save(service);

        log.info("Service supprimé avec succès (soft delete) - ID: {}", id);
    }

    /**
     * Activer/désactiver un service
     */
    @Transactional(readOnly = true)
    @Override
    public ServiceResponse activerService(Long id, boolean actif) {
        log.debug("{} du service ID: {}", actif ? "Activation" : "Désactivation", id);

        var service = serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("SERVICE_NOT_FOUND",
                        "Service non trouvé avec l'ID: " + id));

        service.setIsDeleted(actif);
        var updatedService = serviceRepository.save(service);

        log.info("Service {} avec succès - ID: {}", actif ? "activé" : "désactivé", id);

        return mapToResponse(updatedService);
    }

    /**
     * Vérifier si un service existe
     */
    @Transactional(readOnly = true)
    @Override
    public boolean existsById(Long id) {
        return serviceRepository.findByIdAndIsDeletedFalse(id).isPresent();
    }

    /**
     * Map entity vers response
     */
    private ServiceResponse mapToResponse(Services service) {
        return new ServiceResponse(
                service.getId(),
//                service.getCode(),
                service.getNom(),
                service.getIsDeleted()
//                service.getDescription(),
//                service.getDureeMinutes(),
//                service.getPrix(),
//                service.getEstActif()
        );
    }
}
