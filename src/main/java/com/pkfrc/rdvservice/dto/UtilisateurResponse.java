package com.pkfrc.rdvservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.enumeration.Role;

import java.time.LocalDate;

public record UtilisateurResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("email")
        String email,

        @JsonProperty("username")
        String username,

        @JsonProperty("telephone")
        String telephone,

        @JsonProperty("dateNaissance")
        LocalDate dateNaissance,

        @JsonProperty("role")
        Role role,

        @JsonProperty("isDeleted")
        Boolean isDeleted
) {}