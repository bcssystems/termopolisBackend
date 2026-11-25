package com.bcsystems.siga.domain;

import com.bcsystems.siga.domain.en.EstadoPersonalizacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "personalizacion", indexes = {
        @Index(name = "idx_personalizacion_estado_fecha", columnList = "estado, fecha_creacion"),
        @Index(name = "idx_personalizacion_producto", columnList = "id_producto")
})
public class Personalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPersonalizacion;

    @Column(nullable = false, unique = true, length = 40)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPersonalizacion estado;

    @Column(name = "sku_producto", nullable = false, length = 50)
    private String skuProducto;

    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    @Column(name = "modelo_3d_url", nullable = false, length = 500)
    private String modelo3dUrl;

    @Column(name = "color_nombre", length = 80)
    private String colorNombre;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;

    @Column(name = "capas_json", columnDefinition = "LONGTEXT", nullable = false)
    private String capasJson;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(length = 500)
    private String nota;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "personalizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC, idVista ASC")
    @Builder.Default
    private List<PersonalizacionVista> vistas = new ArrayList<>();
}
