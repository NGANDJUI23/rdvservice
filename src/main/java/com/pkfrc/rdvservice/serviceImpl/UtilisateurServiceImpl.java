package com.pkfrc.rdvservice.serviceImpl;

import com.pkfrc.rdvservice.dto.UtilisateurRequest;
import com.pkfrc.rdvservice.dto.UtilisateurResponse;
import com.pkfrc.rdvservice.enumeration.Role;
import com.pkfrc.rdvservice.entity.Utilisateur;
import com.pkfrc.rdvservice.exception.BusinessException;
import com.pkfrc.rdvservice.exception.ResourceNotFoundException;
import com.pkfrc.rdvservice.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurServiceImpl {

    @Autowired
    UtilisateurRepository utilisateurRepository;
//    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UtilisateurResponse creerUtilisateur(UtilisateurRequest request) {
        log.debug("Création de l'utilisateur avec username: {}", request.username());

        if (utilisateurRepository.existsByUsernameAndIsDeletedFalse(request.username())) {
            throw new BusinessException("USERNAME_EXISTS",
                    "Le nom d'utilisateur " + request.username() + " existe déjà");
        }

        if (utilisateurRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new BusinessException("EMAIL_EXISTS",
                    "L'email " + request.email() + " existe déjà");
        }

        if (utilisateurRepository.existsByTelephoneAndIsDeletedFalse(request.telephone())) {
            throw new BusinessException("TELEPHONE_EXISTS",
                    "Le numero " + request.telephone() + " existe déjà avec un autre compte");
        }

        var utilisateur = Utilisateur.builder()
                .email(request.email())
                .username(request.username())
//                .password(passwordEncoder.encode(request.password()))
                .password(request.password())
                .telephone(request.telephone())
                .dateNaissance(request.dateNaissance())
                .role(request.role())
                .isDeleted(false)
                .build();

        var savedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur créé avec succès - ID: {}, Username: {}",
                savedUtilisateur.getId(), savedUtilisateur.getUsername());

        return mapToResponse(savedUtilisateur);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse getUtilisateurById(Long id) {
        log.debug("Recherche de l'utilisateur avec ID: {}", id);

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByIdAndIsDeletedFalse(id);
        Utilisateur utilisateur = utilisateurOptional
//                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        return mapToResponse(utilisateur);
    }
    /**
     * Récupérer tous les utilisateurs (non supprimés)
     */
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getAllUtilisateurs() {
        log.debug("Récupération de tous les utilisateurs non supprimés");

        List<Utilisateur> utilisateurs = utilisateurRepository.findAllIsDeletedFalse();

        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    /**
     * Récupérer tous les utilisateurs (y compris supprimés)
     */
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getAllUtilisateursIncludingDeleted() {
        log.debug("Récupération de tous les utilisateurs (y compris supprimés)");

        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void supprimerUtilisateur(Long id) {
        log.debug("Suppression soft de l'utilisateur ID: {}", id);

        Utilisateur utilisateur = utilisateurRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "Utilisateur non trouvé avec l'ID: " + id));

        utilisateur.setIsDeleted(true);
        utilisateurRepository.save(utilisateur);

        log.info("Utilisateur supprimé avec succès (soft delete) - ID: {}", id);
    }

    private UtilisateurResponse mapToResponse(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getUsername(),
                utilisateur.getTelephone(),
                utilisateur.getDateNaissance(),
                utilisateur.getRole(),
                utilisateur.getIsDeleted()
        );
    }

    @Transactional
    public UtilisateurResponse updateUtilisateur(Long id, UtilisateurRequest request) {
        log.debug("Mise à jour de l'utilisateur avec ID: {}", id);

        // 1. Vérifier si l'utilisateur existe et n'est pas supprimé
        Utilisateur utilisateur = utilisateurRepository.findActiveById(id).get();
//                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
//                        "Utilisateur non trouvé avec l'ID: " + id));

        // 2. Vérifier l'unicité du username (si changé)
        if (!utilisateur.getUsername().equals(request.username())) {
            if (utilisateurRepository.existsByUsernameAndIsDeletedFalse(request.username())) {
                throw new BusinessException("USERNAME_EXISTS",
                        "Le nom d'utilisateur '" + request.username() + "' est déjà utilisé par un autre utilisateur");
            }
            utilisateur.setUsername(request.username());
        }

        // 3. Vérifier l'unicité de l'email (si changé)
        if (!utilisateur.getEmail().equals(request.email())) {
            if (utilisateurRepository.existsByEmailAndIsDeletedFalse(request.email())) {
                throw new BusinessException("EMAIL_EXISTS",
                        "L'email '" + request.email() + "' est déjà utilisé par un autre utilisateur");
            }
            utilisateur.setEmail(request.email());
        }

        if (utilisateurRepository.existsByTelephoneAndIsDeletedFalse(request.telephone())) {
            throw new BusinessException("TELEPHONE_EXISTS",
                    "Le numero " + request.telephone() + " existe déjà avec un autre compte");
        }

        // 4. Mettre à jour les champs
        utilisateur.setTelephone(request.telephone());
        utilisateur.setDateNaissance(request.dateNaissance());
        utilisateur.setRole(request.role());

        // 5. Mettre à jour le mot de passe seulement s'il est fourni et non vide
        if (request.password() != null && !request.password().trim().isEmpty()) {
//            utilisateur.setPassword(passwordEncoder.encode(request.password()));
            utilisateur.setPassword(request.password());
            log.debug("Mot de passe mis à jour pour l'utilisateur ID: {}", id);
        }

        // 6. Sauvegarder les modifications
        var updatedUtilisateur = utilisateurRepository.save(utilisateur);
        log.info("Utilisateur mis à jour avec succès - ID: {}, Username: {}",
                updatedUtilisateur.getId(), updatedUtilisateur.getUsername());

        return mapToResponse(updatedUtilisateur);
    }

    /**
     * Récupérer les utilisateurs par rôle
     * @param role Le rôle à filtrer (ex: "ADMIN", "CLIENT", "RESPONSABLE")
     * @return Liste des utilisateurs ayant ce rôle
     */
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getUtilisateursByRole(Role role) {
        log.debug("Recherche des utilisateurs par rôle: {}", role);

        if (role == null) {
            throw new BusinessException("ROLE_INVALIDE",
                    "Le rôle ne peut pas être vide");
        }

        List<Utilisateur> utilisateurs = utilisateurRepository.findByRoleAndIsDeletedFalse(role);

        if (utilisateurs.isEmpty()) {
            log.debug("Aucun utilisateur trouvé avec le rôle: {}", role);
        } else {
            log.debug("{} utilisateur(s) trouvé(s) avec le rôle: {}", utilisateurs.size(), role);
        }

        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les utilisateurs par rôle (sensible à la casse)
     * @param role Le rôle à filtrer
     * @return Liste des utilisateurs ayant ce rôle
     */
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getUtilisateursByRoleIgnoreCase(Role role) {
        log.debug("Recherche des utilisateurs par rôle (ignore case): {}", role);

        List<Utilisateur> utilisateurs = utilisateurRepository.findByRoleIgnoreCaseAndIsDeletedFalse(role);

        return utilisateurs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les utilisateurs par rôle
     * @param role Le rôle à compter
     * @return Nombre d'utilisateurs ayant ce rôle
     */
    @Transactional(readOnly = true)
    public long countUtilisateursByRole(Role role) {
        log.debug("Comptage des utilisateurs par rôle: {}", role);

        return utilisateurRepository.countByRoleAndIsDeletedFalse(role);
    }

    /**
     * Vérifier si des utilisateurs existent avec un rôle donné
     * @param role Le rôle à vérifier
     * @return true si au moins un utilisateur existe avec ce rôle
     */
    @Transactional(readOnly = true)
    public boolean hasUtilisateursWithRole(Role role) {
        log.debug("Vérification de l'existence d'utilisateurs avec le rôle: {}", role);

        return countUtilisateursByRole(role) > 0;
    }

    /**
     * Recherche un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé
     */
    @Transactional(readOnly = true)
    public UtilisateurResponse getUtilisateurByEmail(String email) {
        log.debug("Recherche de l'utilisateur par email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("EMAIL_INVALIDE",
                    "L'email ne peut pas être vide");
        }

        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmailAndIsDeletedFalse(email);
        Utilisateur utilisateur = utilisateurOptional
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        log.debug("Utilisateur trouvé: {} - {}", utilisateur.getUsername(), utilisateur.getEmail());

        return mapToResponse(utilisateur);
    }

    /**
     * Recherche un utilisateur par son username
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé
     */
    @Transactional(readOnly = true)
    public UtilisateurResponse getUtilisateurByUsername(String username) {
        log.debug("Recherche de l'utilisateur par username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("USERNAME_INVALIDE",
                    "Le nom d'utilisateur ne peut pas être vide");
        }

        Utilisateur utilisateur = utilisateurRepository.findByUsernameAndIsDeletedFalse(username).get();
//                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));
        log.debug("Utilisateur trouvé: {} - {}", utilisateur.getUsername(), utilisateur.getEmail());

        return mapToResponse(utilisateur);
    }

}
