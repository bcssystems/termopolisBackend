package com.bcsystems.siga.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto_color", indexes = {
        @Index(name = "idx_producto_color_producto_orden", columnList = "id_producto, orden")
})
public class ProductoColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idColor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 7)
    private String hex;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;
}
