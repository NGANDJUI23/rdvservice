package com.pkfrc.rdvservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter
@Table(name = "utilisateur")
@SuperBuilder
public class Utilisateur extends Abstract{
    @Column
    private String email;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String telephone;
    @Column
    private LocalDate dateNaissance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

}
