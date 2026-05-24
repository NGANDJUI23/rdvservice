package com.pkfrc.rdvservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.enumeration.NomService;
import jakarta.validation.constraints.*;

public record ServiceRequest(

        @NotNull(message = "Le nom du service est requis")
        @JsonProperty("nom")
        NomService nom

//        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
//        @JsonProperty("description")
//        String description,

//        @NotNull(message = "La durée est requise")
//        @Positive(message = "La durée doit être positive")
//        @JsonProperty("dureeMinutes")
//        Integer dureeMinutes,

//        @PositiveOrZero(message = "Le prix doit être positif ou nul")
//        @JsonProperty("prix")
//        Double prix,
//
//        @JsonProperty("estActif")
//        Boolean estActif
) {}
