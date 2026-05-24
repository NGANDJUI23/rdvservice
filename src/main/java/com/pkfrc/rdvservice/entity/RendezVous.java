package com.pkfrc.rdvservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Entity
@Table(name = "rdv")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@SuperBuilder
public class RendezVous extends Abstract {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_service", nullable = false)
    private Services service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_responsable", nullable = false)
    private Utilisateur responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_client", nullable = false)
    private Utilisateur client;

    @Column(name = "date_rdv", nullable = false)
    private LocalDateTime dateRdv;

    @Column(name = "motif_rdv", nullable = false)
    private String motifRdv;


    @Version
    private long version;

}
