-- Migración Termopolis: configuración de termos personalizables y diseños guardados.
-- Respaldar la base antes de ejecutarla. Revisar primero con SHOW COLUMNS.

ALTER TABLE producto
    ADD COLUMN es_personalizable BIT NOT NULL DEFAULT 0,
    ADD COLUMN capacidad_ml INT NULL,
    ADD COLUMN material VARCHAR(80) NULL,
    ADD COLUMN tipo_tapa VARCHAR(80) NULL,
    ADD COLUMN precio_personalizacion DOUBLE NOT NULL DEFAULT 0;

ALTER TABLE producto_aud
    ADD COLUMN es_personalizable BIT NULL,
    ADD COLUMN capacidad_ml INT NULL,
    ADD COLUMN material VARCHAR(80) NULL,
    ADD COLUMN tipo_tapa VARCHAR(80) NULL,
    ADD COLUMN precio_personalizacion DOUBLE NULL;

CREATE TABLE producto_color (
    id_color INT NOT NULL AUTO_INCREMENT,
    id_producto INT NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    hex VARCHAR(7) NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    orden INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id_color),
    CONSTRAINT fk_producto_color_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    INDEX idx_producto_color_producto_orden (id_producto, orden)
);

CREATE TABLE personalizacion (
    id_personalizacion INT NOT NULL AUTO_INCREMENT,
    folio VARCHAR(40) NOT NULL,
    id_producto INT NOT NULL,
    id_cliente INT NULL,
    id_usuario INT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    sku_producto VARCHAR(50) NOT NULL,
    nombre_producto VARCHAR(200) NOT NULL,
    modelo_3d_url VARCHAR(500) NOT NULL,
    color_nombre VARCHAR(80) NULL,
    color_hex VARCHAR(7) NOT NULL,
    capas_json LONGTEXT NOT NULL,
    precio_unitario DOUBLE NOT NULL,
    cantidad DECIMAL(19,2) NOT NULL,
    fecha_entrega DATE NULL,
    nota VARCHAR(500) NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_actualizacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id_personalizacion),
    UNIQUE KEY uk_personalizacion_folio (folio),
    CONSTRAINT fk_personalizacion_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT fk_personalizacion_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
    CONSTRAINT fk_personalizacion_usuario FOREIGN KEY (id_usuario) REFERENCES persona (id_persona),
    INDEX idx_personalizacion_estado_fecha (estado, fecha_creacion),
    INDEX idx_personalizacion_producto (id_producto)
);

CREATE TABLE personalizacion_vista (
    id_vista INT NOT NULL AUTO_INCREMENT,
    id_personalizacion INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    url VARCHAR(500) NOT NULL,
    orden INT NOT NULL,
    PRIMARY KEY (id_vista),
    CONSTRAINT fk_personalizacion_vista_personalizacion FOREIGN KEY (id_personalizacion)
        REFERENCES personalizacion (id_personalizacion) ON DELETE CASCADE,
    INDEX idx_personalizacion_vista_personalizacion (id_personalizacion, orden)
);

ALTER TABLE venta
    ADD COLUMN fecha_entrega DATE NULL;

ALTER TABLE venta_aud
    ADD COLUMN fecha_entrega DATE NULL;

ALTER TABLE venta_detalle
    ADD COLUMN id_personalizacion INT NULL,
    ADD CONSTRAINT fk_venta_detalle_personalizacion FOREIGN KEY (id_personalizacion)
        REFERENCES personalizacion (id_personalizacion);
