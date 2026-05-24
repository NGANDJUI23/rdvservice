package com.pkfrc.rdvservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.entity.NomService;

public record ServiceResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("nom")
        NomService nom,

        @JsonProperty("isDeleted")
        Boolean isDeleted

//        @JsonProperty("description")
//        String description,

//        @JsonProperty("dureeMinutes")
//        Integer dureeMinutes,
//
//        @JsonProperty("prix")
//        Double prix,

//        @JsonProperty("estActif")
//        Boolean estActif
) {}