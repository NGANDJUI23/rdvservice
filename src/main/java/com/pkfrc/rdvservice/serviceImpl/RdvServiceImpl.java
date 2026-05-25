package com.pkfrc.rdvservice.serviceImpl;

import com.pkfrc.rdvservice.dto.RendezVousRequest;
import com.pkfrc.rdvservice.dto.RendezVousResponse;
import com.pkfrc.rdvservice.entity.RendezVous;
import com.pkfrc.rdvservice.entity.Services;
import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.enumeration.StatutRDV;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.repository.RendezVousRepository;
import com.pkfrc.rdvservice.repository.ServiceRepository;
import com.pkfrc.rdvservice.repository.UtilisateurRepository;
import com.pkfrc.rdvservice.serviceFace.RdvServiceFace;
import com.pkfrc.rdvservice.util.DateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RdvServiceImpl implements RdvServiceFace {

    @Autowired
    RendezVousRepository rendezVousRepository;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    ServiceRepository serviceRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    @Override
    public RendezVousResponse creerRendezVous(RendezVousRequest request) {
        log.debug("Création du rendez-vous pour le client: {} au service: {}",
                request.refClient(), request.refService());

        // 1. Validation de la date (au moins 2 jours avant)
        if (!DateValidator.isValidAppointmentDate(request.dateRDV())) {
            long daysUntil = DateValidator.getDaysUntilAppointment(request.dateRDV());
            LocalDateTime minDate = DateValidator.getMinAppointmentDate();

            throw new BusinessException("DATE_TROP_PROCHES",
                    String.format("Le rendez-vous doit être pris au moins 2 jours avant sa date. " +
                                    "Date demandée: %s. Date minimale acceptable: %s. " +
                                    "Il reste seulement %d jour(s) avant le rendez-vous.",
                            request.dateRDV(), minDate, daysUntil));
        }


        // Vérifier l'existence du client
        var client = utilisateurRepository.findById(request.refClient())
                .orElseThrow(() -> {
                    log.warn("Client non trouvé avec l'ID: {}", request.refClient());
                    return new BusinessException("CLIENT_NOT_FOUND",
                            "Client non trouvé avec l'ID: " + request.refClient());
                });

        // Vérifier l'existence du service
        Services service = serviceRepository.findByIdAndIsDeletedFalse(request.refService())
                .orElseThrow(() -> {
                    log.warn("Service non trouvé avec l'ID: {}", request.refService());
                    return new BusinessException("SERVICE_NOT_FOUND",
                            "Service non trouvé avec l'ID: " + request.refService());
                });

        // 4. Calculer la plage horaire du nouveau rendez-vous (début + 1 heure)
        LocalDateTime newStartTime = request.dateRDV();
        LocalDateTime newEndTime = DateValidator.calculateEndTime(newStartTime);

        log.debug("Nouveau RDV: début={}, fin={}", newStartTime, newEndTime);

        // 4. Calculer la plage horaire du nouveau rendez-vous
        LocalDateTime newStart = request.dateRDV();
        LocalDateTime newEnd = newStart.plusHours(1); // Durée fixe de 1 heure

        log.debug("Nouveau RDV: début={}, fin={}", newStart, newEnd);


        // 5. VÉRIFICATION 1: Le client a-t-il déjà un rendez-vous qui chevauche cette plage ?
        List<RendezVous> clientAppointments = rendezVousRepository.findActiveAppointmentsByClient(request.refClient());

        for (RendezVous existing : clientAppointments) {
            LocalDateTime existingStart = existing.getDateRdv();
            LocalDateTime existingEnd = existingStart.plusHours(1);

            boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);

            if (hasOverlap) {
                throw new BusinessException("CONFLIT_HORAIRE_CLIENT",
                        String.format("Vous avez déjà un rendez-vous le %s de %s à %s (service: %s). " +
                                        "Impossible de créer un nouveau rendez-vous sur cette plage horaire.",
                                existingStart.format(DATE_FORMATTER),
                                existingStart.format(TIME_FORMATTER),
                                existingEnd.format(TIME_FORMATTER),
                                existing.getService().getNom()));
            }
        }

        // 6. VÉRIFICATION 2: Le responsable est-il disponible sur cette plage horaire ?
        List<RendezVous> responsableAppointments = rendezVousRepository.findByResponsableId(request.refResponsable());

        for (RendezVous existing : responsableAppointments) {
            LocalDateTime existingStart = existing.getDateRdv();
            LocalDateTime existingEnd = existingStart.plusHours(1);

            boolean hasOverlap = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);

            if (hasOverlap) {
                throw new BusinessException("RESPONSABLE_OCCUPE",
                        String.format("Le responsable n'est pas disponible. Il a déjà un rendez-vous le %s de %s à %s (service: %s, client: %s %s)",
                                existingStart.format(DATE_FORMATTER),
                                existingStart.format(TIME_FORMATTER),
                                existingEnd.format(TIME_FORMATTER),
                                existing.getService().getNom(),
                                existing.getClient().getUsername()));
            }
        }

        log.debug("Aucun conflit trouvé pour le client {}", request.refClient());


        // Vérifier l'existence du responsable
        Optional<Utilisateur> responsable = Optional.of(utilisateurRepository.findByIdAndIsDeletedFalse(request.refResponsable())
                .orElseThrow(() -> {
                    log.warn("Responsable non trouvé avec l'ID: {}", request.refResponsable());
                    return new BusinessException("RESPONSABLE_NOT_FOUND",
                            "Responsable non trouvé avec l'ID: " + request.refResponsable());
                }));

        // Vérifier que le responsable a le bon rôle
        if (!"RESPONSABLE".equals(responsable.get().getRole().toString())) {
            throw new BusinessException("ROLE_INVALIDE",
                    "L'utilisateur " + responsable.get().getUsername() + " n'a pas le rôle de responsable");
        }

        // Contrôle de concurrence: Vérifier si le créneau est déjà pris
        Optional<RendezVous> existingRendezVous = rendezVousRepository.findByServiceIdAndDateRdvWithLockAndClientIdAndResponsableId(
                request.refService(), request.dateRDV(), request.refClient(), request.refResponsable());

        if (existingRendezVous.isPresent()) {
            log.warn("Créneau horaire déjà occupé pour le service ID {} à {}",
                    request.refService(), request.dateRDV());
            throw new BusinessException("CRENEAU_INDISPONIBLE",
                    "Le créneau horaire pour le service " + service.getNom() +
                            " à " + request.dateRDV() + " est déjà réservé");
        }

        // Double vérification
        if (rendezVousRepository.isTimeSlotTaken(request.refService(), request.dateRDV(), request.refResponsable(), request.refClient())) {
            throw new BusinessException("CRENEAU_INDISPONIBLE",
                    "Le créneau horaire est déjà pris");
        }

        // Créer le rendez-vous
        var rendezVous = RendezVous.builder()
                .dateRdv(request.dateRDV())
                .motifRdv(request.motifRdv())
                .service(service)
                .responsable(responsable.get())
                .client(client)
                .isDeleted(false)
                .statut(StatutRDV.PLANIFIE)
                .build();

        try {
            RendezVous savedRendezVous = rendezVousRepository.save(rendezVous);
            log.info("Rendez-vous créé avec succès - ID: {}, Client: {}, Service: {}",
                    savedRendezVous.getId(),
                    client.getUsername(),
                    service.getId());

            return mapToResponse(savedRendezVous);
        } catch (Exception e) {
            log.error("Échec de la création du rendez-vous: {}", e.getMessage());
            throw new BusinessException("CREATION_RDV_ECHEC",
                    "Impossible de créer le rendez-vous en raison d'une modification concurrente");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public RendezVousResponse getRendezVousById(Long id) {
        log.debug("Recherche du rendez-vous avec ID: {}", id);

        var rendezVous = rendezVousRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("Rendez-vous non trouvé avec l'ID: {}", id);
                    return new BusinessException("RDV_NOT_FOUND",
                            "Rendez-vous non trouvé avec l'ID: " + id);
                });

        return mapToResponse(rendezVous);
    }

    @Transactional
    @Override
    public void annulerRendezVous(Long id, String motifAnnulation) {
        log.debug("Annulation du rendez-vous ID: {}", id);

        RendezVous rendezVous = rendezVousRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("RDV_NOT_FOUND",
                        "Rendez-vous non trouvé avec l'ID: " + id));

        if (rendezVous.getDateRdv().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("RDV_PASSE",
                    "Impossible d'annuler un rendez-vous passé");
        }

        rendezVous.setStatut(StatutRDV.ANNULE);
        rendezVousRepository.save(rendezVous);

        log.info("Rendez-vous annulé avec succès - ID: {}, Motif: {}", id, motifAnnulation);
    }

    @Transactional
    @Override
    public RendezVousResponse effectuerRendezVous(Long id) {
        log.debug("Annulation du rendez-vous ID: {}", id);

        RendezVous rendezVous = rendezVousRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException("RDV_NOT_FOUND",
                        "Rendez-vous non trouvé avec l'ID: " + id));

        if (rendezVous.getDateRdv().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("RDV_PASSE",
                    "Impossible d'effectuer un rendez-vous passé");
        }

        if (rendezVous.getStatut().equals(StatutRDV.ANNULE)) {
            throw new BusinessException("RDV_PASSE",
                    "Impossible d'effectuer un rendez-vous annule");
        }

        rendezVous.setStatut(StatutRDV.EFFECTUE);
        RendezVous rendezVousSaved = rendezVousRepository.save(rendezVous);


        log.info("Rendez-vous effectue avec succès - ID: {}", id);
        return mapToResponse(rendezVousSaved);
    }

    private RendezVousResponse mapToResponse(RendezVous rendezVous) {
        return new RendezVousResponse(
                rendezVous.getClient().getId(),     // refClient
                rendezVous.getId(),                                  // refRDV
                rendezVous.getService().getId(),                   // refService
                rendezVous.getResponsable().getId(), // refResponsable
                rendezVous.getDateRdv(),                             // dateRDV
                rendezVous.getMotifRdv(), // motifRdv
                rendezVous.getIsDeleted(),
                rendezVous.getStatut()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVousResponse> getRendezVousByClient(Long clientId) {
        log.debug("Recherche des rendez-vous du client: {}", clientId);

        utilisateurRepository.findActiveById(clientId)
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND",
                        "Client non trouvé avec l'ID: " + clientId));

        var rendezVousList = rendezVousRepository.findByClientId(clientId);

        return rendezVousList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<RendezVousResponse> getRendezVousByResponsable(Long responsableId) {
        log.debug("Recherche des rendez-vous du responsable: {}", responsableId);

        utilisateurRepository.findActiveById(responsableId)
                .orElseThrow(() -> new BusinessException("RESPONSABLE_NOT_FOUND",
                        "Responsable non trouvé avec l'ID: " + responsableId));

        var rendezVousList = rendezVousRepository.findByResponsableId(responsableId);

        return rendezVousList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> listRentezvous() {

        var rendezVousList = rendezVousRepository.findAll();

        return rendezVousList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
