package com.pkfrc.rdvservice.entity;

import com.pkfrc.rdvservice.enumeration.NomService;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "service")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
@SuperBuilder
public class Services extends Abstract{
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NomService nom;
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    private List<RendezVous> rdvList;
}
