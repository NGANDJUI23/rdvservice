package com.pkfrc.rdvservice.api;

import com.pkfrc.rdvservice.dto.UtilisateurRequest;
import com.pkfrc.rdvservice.dto.UtilisateurResponse;
import com.pkfrc.rdvservice.enumeration.Role;
import com.pkfrc.rdvservice.serviceImpl.UtilisateurServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UtilisateurApi extends BaseApi {

    @Autowired
    UtilisateurServiceImpl utilisateurService;

    /**
     * Créer un nouvel utilisateur
     * POST /api/utilisateurs
     */

    @PostMapping
    @Operation(summary = "Créer un nouvel utilisateur", description = "Crée un nouveau utilisateur dans le système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès",
                    content = @Content(schema = @Schema(implementation = UtilisateurResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "409", description = "Email ou username déjà existant")
    })
    public ResponseEntity<UtilisateurResponse> creerUtilisateur(@Valid @RequestBody UtilisateurRequest request) {
        log.info("Requête de création d'utilisateur: {}", request.username());
        UtilisateurResponse response = utilisateurService.creerUtilisateur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un utilisateur par son ID
     * GET /api/utilisateurs/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un utilisateur par ID", description = "Retourne un utilisateur selon son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<UtilisateurResponse> getUtilisateurById(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable Long id) {
        log.info("Requête de récupération d'utilisateur avec ID: {}", id);
        UtilisateurResponse response = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les utilisateurs
     * GET /api/utilisateurs
     */
    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs", description = "Retourne la liste de tous les utilisateurs actifs")
    public ResponseEntity<List<UtilisateurResponse>> getAllUtilisateurs() {
        log.info("Requête de récupération de tous les utilisateurs");
        List<UtilisateurResponse> responses = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(responses);
    }

    /**
     * Mettre à jour un utilisateur
     * PUT /api/utilisateurs/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Met à jour les informations d'un utilisateur")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequest request) {
        log.info("Requête de mise à jour d'utilisateur avec ID: {}", id);
        UtilisateurResponse response = utilisateurService.updateUtilisateur(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un utilisateur (soft delete)
     * DELETE /api/utilisateurs/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime logiquement un utilisateur")
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès")
    public ResponseEntity<Void> supprimerUtilisateur(
            @Parameter(description = "ID de l'utilisateur", required = true)
            @PathVariable Long id
    ) {
        log.info("Requête de suppression d'utilisateur avec ID: {}", id);
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Rechercher des utilisateurs par rôle
     * GET /api/utilisateurs/role/{role}
     */
    @GetMapping("/role/{role}")
    @Operation(summary = "Lister les utilisateurs par rôle", description = "Retourne la liste des utilisateurs selon leur rôle")
    public ResponseEntity<List<UtilisateurResponse>> getUtilisateursByRole(
            @Parameter(description = "Rôle (ADMIN, CLIENT, RESPONSABLE)", required = true)
            @PathVariable Role role) {
        log.info("Requête de recherche d'utilisateurs par rôle: {}", role);
        List<UtilisateurResponse> responses = utilisateurService.getUtilisateursByRole(role);
        return ResponseEntity.ok(responses);
    }

    /**
     * Rechercher un utilisateur par email
     * GET /api/utilisateurs/email/{email}
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Récupérer un utilisateur par email", description = "Retourne un utilisateur selon son email")
    public ResponseEntity<UtilisateurResponse> getUtilisateurByEmail(
            @Parameter(description = "Email de l'utilisateur", required = true)
            @PathVariable String email) {
        log.info("Requête de récupération d'utilisateur par email: {}", email);
        UtilisateurResponse response = utilisateurService.getUtilisateurByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher un utilisateur par username
     * GET /api/utilisateurs/username/{username}
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Récupérer un utilisateur par username", description = "Retourne un utilisateur selon son nom d'utilisateur")
    public ResponseEntity<UtilisateurResponse> getUtilisateurByUsername(@PathVariable String username) {
        log.info("Requête de recherche d'utilisateur par username: {}", username);
        UtilisateurResponse response = utilisateurService.getUtilisateurByUsername(username);
        return ResponseEntity.ok(response);
    }
}
