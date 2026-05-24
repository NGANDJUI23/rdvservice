package com.pkfrc.rdvservice.api;

import com.pkfrc.rdvservice.dto.RendezVousRequest;
import com.pkfrc.rdvservice.dto.RendezVousResponse;
import com.pkfrc.rdvservice.serviceImpl.RdvServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rendez-vous", description = "API de gestion des rendez-vous")
public class RendezVousApi extends BaseApi {

    @Autowired
    RdvServiceImpl rendezVousService;

    /**
     * Créer un nouveau rendez-vous
     * POST /api/rendez-vous
     */
    @PostMapping
    @Operation(summary = "Créer un rendez-vous", description = "Crée un nouveau rendez-vous avec vérification de disponibilité")
    public ResponseEntity<RendezVousResponse> creerRendezVous(@Valid @RequestBody RendezVousRequest request) {
        log.info("Requête de création de rendez-vous pour le client: {}", request.refClient());
        RendezVousResponse response = rendezVousService.creerRendezVous(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un rendez-vous par son ID
     * GET /api/rendez-vous/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un rendez-vous", description = "Retourne un rendez-vous selon son ID")
    public ResponseEntity<RendezVousResponse> getRendezVousById(
            @Parameter(description = "ID du rendez-vous", required = true)
            @PathVariable Long id) {
        log.info("Requête de récupération du rendez-vous avec ID: {}", id);
        RendezVousResponse response = rendezVousService.getRendezVousById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les rendez-vous d'un client
     * GET /api/rendez-vous/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    @Operation(summary = "Rendez-vous d'un client", description = "Retourne tous les rendez-vous d'un client")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousByClient(
            @Parameter(description = "ID du client", required = true)
            @PathVariable Long clientId) {
        log.info("Requête de récupération des rendez-vous du client: {}", clientId);
        List<RendezVousResponse> responses = rendezVousService.getRendezVousByClient(clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer tous les rendez-vous d'un responsable
     * GET /api/rendez-vous/responsable/{responsableId}
     */
    @GetMapping("/responsable/{responsableId}")
    @Operation(summary = "Rendez-vous d'un responsable", description = "Retourne tous les rendez-vous d'un responsable")
    public ResponseEntity<List<RendezVousResponse>> getRendezVousByResponsable(
            @Parameter(description = "ID du rendez-vous", required = true)
            @PathVariable Long responsableId) {
        log.info("Requête de récupération des rendez-vous du responsable: {}", responsableId);
        List<RendezVousResponse> responses = rendezVousService.getRendezVousByResponsable(responsableId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Annuler un rendez-vous
     * DELETE /api/rendez-vous/{id}?motif=...
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un rendez-vous", description = "Annule un rendez-vous existant")
    public ResponseEntity<Void> annulerRendezVous(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Annulation par l'utilisateur") String motif) {
        log.info("Requête d'annulation du rendez-vous ID: {}, Motif: {}", id, motif);
        rendezVousService.annulerRendezVous(id, motif);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirmer un rendez-vous
     * PATCH /api/rendez-vous/{id}/confirmer
     */
//    @PatchMapping("/{id}/confirmer")
//    @Operation(summary = "Confirmer un rendez-vous", description = "Confirme un rendez-vous planifié")
//    public ResponseEntity<RendezVousResponse> confirmerRendezVous(@PathVariable Long id) {
//        log.info("Requête de confirmation du rendez-vous ID: {}", id);
//        RendezVousResponse response = rendezVousService.confirmerRendezVous(id);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Marquer un rendez-vous comme effectué
     * PATCH /api/rendez-vous/{id}/effectuer
     */
//    @PatchMapping("/{id}/effectuer")
//    public ResponseEntity<RendezVousResponse> effectuerRendezVous(@PathVariable Long id) {
//        log.info("Requête pour marquer le rendez-vous ID: {} comme effectué", id);
//        RendezVousResponse response = rendezVousService.effectuerRendezVous(id);
//        return ResponseEntity.ok(response);
//    }
}