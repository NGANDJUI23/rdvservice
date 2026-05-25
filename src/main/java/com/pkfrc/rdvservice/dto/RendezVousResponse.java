package com.pkfrc.rdvservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.enumeration.StatutRDV;

import java.time.LocalDateTime;

public record RendezVousResponse(
        @JsonProperty("refClient")
        Long refClient,

        @JsonProperty("refRDV")
        Long refRDV,

        @JsonProperty("refService")
        Long refService,

        @JsonProperty("refResponsable")
        Long refResponsable,

        @JsonProperty("dateRDV")
        LocalDateTime dateRDV,

        @JsonProperty("motifRdv")
        String motifRdv,

        @JsonProperty("isDeleted")
        Boolean isDeleted,

        @JsonProperty("statut")
        StatutRDV statut
) {}
