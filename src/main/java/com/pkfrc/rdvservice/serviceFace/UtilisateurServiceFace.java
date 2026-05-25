package com.pkfrc.rdvservice.serviceFace;

import com.pkfrc.rdvservice.dto.UtilisateurRequest;
import com.pkfrc.rdvservice.dto.UtilisateurResponse;
import com.pkfrc.rdvservice.enumeration.Role;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


 public interface UtilisateurServiceFace {
     UtilisateurResponse creerUtilisateur(UtilisateurRequest request);
    
     UtilisateurResponse getUtilisateurById(Long id);

    /**
     * Récupérer tous les utilisateurs (non supprimés)
     */
    
     List<UtilisateurResponse> getAllUtilisateurs();

    /**
     * Récupérer tous les utilisateurs (y compris supprimés)
     */
    
     List<UtilisateurResponse> getAllUtilisateursIncludingDeleted();

     void supprimerUtilisateur(Long id);

     UtilisateurResponse updateUtilisateur(Long id, UtilisateurRequest request);

    /**
     * Récupérer les utilisateurs par rôle
     * @param role Le rôle à filtrer (ex: "ADMIN", "CLIENT", "RESPONSABLE")
     * @return Liste des utilisateurs ayant ce rôle
     */
    
     List<UtilisateurResponse> getUtilisateursByRole(Role role);

    /**
     * Récupérer les utilisateurs par rôle (sensible à la casse)
     * @param role Le rôle à filtrer
     * @return Liste des utilisateurs ayant ce rôle
     */
    
     List<UtilisateurResponse> getUtilisateursByRoleIgnoreCase(Role role);

    /**
     * Compter les utilisateurs par rôle
     * @param role Le rôle à compter
     * @return Nombre d'utilisateurs ayant ce rôle
     */
    
     long countUtilisateursByRole(Role role);

    /**
     * Vérifier si des utilisateurs existent avec un rôle donné
     * @param role Le rôle à vérifier
     * @return true si au moins un utilisateur existe avec ce rôle
     */
    
     boolean hasUtilisateursWithRole(Role role);

    /**
     * Recherche un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé
     */
    
     UtilisateurResponse getUtilisateurByEmail(String email);

    /**
     * Recherche un utilisateur par son username
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé
     */
    
     UtilisateurResponse getUtilisateurByUsername(String username);
}
