package com.pkfrc.rdvservice.api;

import com.pkfrc.rdvservice.dto.ServiceRequest;
import com.pkfrc.rdvservice.dto.ServiceResponse;
import com.pkfrc.rdvservice.serviceImpl.ServiceMetierImpl;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Services", description = "API de gestion des differents services (RH, ARCHIVE, DAF, RH, COMPTABILITE, AFFAIRE_SCOLAIRE)")
public class ServicesApi extends BaseApi {

    @Autowired
    ServiceMetierImpl serviceMetier;

    /**
     * Créer un nouveau service
     * POST /api/services
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau service", description = "Crée un nouveau service dans le système")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        log.info("Requête de création de service: {}", request.nom());
        ServiceResponse response = serviceMetier.creerService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un service par son ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "recuperer un service", description = "recuperer un service dans le système")
    public ResponseEntity<ServiceResponse> getService(@PathVariable Long id) {
        log.info("Requête de récupération du service avec ID: {}", id);
        ServiceResponse response = serviceMetier.getServiceById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les services actifs
     * GET /api/services
     */
    @GetMapping
    @Operation(summary = "recuperer tout les services", description = "recuperer tout les services dans le système")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        log.info("Requête de récupération de tous les services actifs");
        List<ServiceResponse> responses = serviceMetier.getAllServicesActifs();
        return ResponseEntity.ok(responses);
    }

    /**
     * Mettre à jour un service
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre a jour un service par son ID", description = "mettre a jour un service dans le système")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        log.info("Requête de mise à jour du service ID: {}", id);
        ServiceResponse response = serviceMetier.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un service (soft delete)
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "supprimer un service", description = "supprimer un service dans le système")
    public ResponseEntity<Void> supprimerService(@PathVariable Long id) {
        log.info("Requête de suppression du service ID: {}", id);
        serviceMetier.supprimerService(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activer/désactiver un service
     * PATCH /api/services/{id}/activer?actif=true
     */
    @PatchMapping("/{id}/activer")
    @Operation(summary = "Activer ou deactiver un service", description = "Activer ou deactiver un service dans le système")
    public ResponseEntity<ServiceResponse> activerService(
            @PathVariable Long id,
            @RequestParam boolean actif) {
        log.info("Requête pour {} le service ID: {}", actif ? "activer" : "désactiver", id);
        ServiceResponse response = serviceMetier.activerService(id, actif);
        return ResponseEntity.ok(response);
    }
}
