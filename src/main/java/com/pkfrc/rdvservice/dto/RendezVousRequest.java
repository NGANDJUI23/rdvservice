package com.pkfrc.rdvservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pkfrc.rdvservice.enumeration.StatutRDV;
import com.pkfrc.rdvservice.util.DateValidator;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record RendezVousRequest(
        @NotNull(message = "La référence du client est requise")
        @JsonProperty("refClient")
        Long refClient,

        @NotNull(message = "La date du RDV est requise")
        @Future(message = "La date du RDV doit être dans le futur")
        @JsonProperty("dateRDV")
        LocalDateTime dateRDV,

        @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères")
        @JsonProperty("motifRdv")
        String motifRdv,

        @NotNull(message = "La référence du service est requise")
        @JsonProperty("refService")
        Long refService,

        @NotNull(message = "La référence du responsable est requise")
        @JsonProperty("refResponsable")
        Long refResponsable,

        @NotNull(message = "Le statut du rendez-vous est requise")
        @JsonProperty("statut")
        StatutRDV statut
) {
}
