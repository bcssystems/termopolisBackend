package com.bcsystems.siga.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio1")
    private Double precioBase;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal stockActual = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal stockMinimo;
    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal stockMaximo;

    private Double costoPromedio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Builder.Default
    @Column(length = 10)
    private String unidadMedida = "UNIDAD";

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "es_personalizable", nullable = false)
    private Boolean esPersonalizable = false;

    @Column(name = "capacidad_ml")
    private Integer capacidadMl;

    @Column(length = 80)
    private String material;

    @Column(name = "tipo_tapa", length = 80)
    private String tipoTapa;

    @Builder.Default
    @Column(name = "precio_personalizacion", nullable = false)
    private Double precioPersonalizacion = 0.0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventarioSucursal> inventarioSucursales = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("orden ASC, idColor ASC")
    @NotAudited
    @Builder.Default
    private List<ProductoColor> colores = new ArrayList<>();
}
