package com.pkfrc.rdvservice.entity;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public class Abstract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @LastModifiedDate
    @Column(name = "Date_Modification")
    public LocalDateTime dateUpdate;

    @CreationTimestamp
    @Column(name = "Date_Creation", nullable = false, updatable = false)
    public LocalDateTime dateCreate;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}