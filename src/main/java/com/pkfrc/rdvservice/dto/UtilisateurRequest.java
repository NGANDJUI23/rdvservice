package com.pkfrc.rdvservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.entity.Role;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UtilisateurRequest(
        @NotBlank(message = "L'email est requis")
        @Email(message = "Format d'email invalide")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Le nom d'utilisateur est requis")
        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        @JsonProperty("username")
        String username,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        @JsonProperty("password")
        String password,

        @NotBlank(message = "Le téléphone est requis")
        @Pattern(regexp = "\\d{10,15}", message = "Le numéro de téléphone doit contenir entre 10 et 15 chiffres")
        @JsonProperty("telephone")
        String telephone,

        @Past(message = "La date de naissance doit être dans le passé")
        @JsonProperty("dateNaissance")
        LocalDate dateNaissance,

        @NotNull(message = "Le rôle est requis")
        @JsonProperty("role")
        Role role
) {}
