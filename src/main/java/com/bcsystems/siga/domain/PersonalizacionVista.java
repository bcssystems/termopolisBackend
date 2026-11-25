package com.bcsystems.siga.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "personalizacion_vista", indexes = {
        @Index(name = "idx_personalizacion_vista_personalizacion", columnList = "id_personalizacion, orden")
})
public class PersonalizacionVista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personalizacion", nullable = false)
    private Personalizacion personalizacion;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Integer orden;
}
