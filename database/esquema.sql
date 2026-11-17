-- Biblioteca Web - Esquema de base de datos (MySQL 8)
-- Ejecutar con: mysql -u root -p < database/esquema.sql

CREATE DATABASE IF NOT EXISTS biblioteca
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE biblioteca;

-- Usuario de la aplicación (cambiar la contraseña)
CREATE USER IF NOT EXISTS 'biblio'@'localhost' IDENTIFIED BY '131122';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblio'@'localhost';
FLUSH PRIVILEGES;

-- Orden inverso por las llaves foráneas
DROP TABLE IF EXISTS prestamo;
DROP TABLE IF EXISTS libro;
DROP TABLE IF EXISTS socio;
DROP TABLE IF EXISTS categoria;

CREATE TABLE categoria (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(80)  NOT NULL UNIQUE,
  descripcion VARCHAR(255)
);

CREATE TABLE libro (
  id                     INT AUTO_INCREMENT PRIMARY KEY,
  titulo                 VARCHAR(200) NOT NULL,
  autor                  VARCHAR(150) NOT NULL,
  isbn                   VARCHAR(20)  NOT NULL UNIQUE,
  anio_publicacion       INT,
  ejemplares_totales     INT NOT NULL DEFAULT 1,
  ejemplares_disponibles INT NOT NULL DEFAULT 1,
  categoria_id           INT NOT NULL,
  CONSTRAINT fk_libro_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id),
  CONSTRAINT ck_libro_ejemplares CHECK (
    ejemplares_totales >= 0
    AND ejemplares_disponibles >= 0
    AND ejemplares_disponibles <= ejemplares_totales
  )
);

CREATE TABLE socio (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(120) NOT NULL,
  correo         VARCHAR(120) NOT NULL UNIQUE,
  telefono       VARCHAR(20),
  estado         VARCHAR(12)  NOT NULL DEFAULT 'ACTIVO',
  fecha_registro DATE         NOT NULL DEFAULT (CURRENT_DATE),
  CONSTRAINT ck_socio_estado CHECK (estado IN ('ACTIVO', 'SUSPENDIDO'))
);

CREATE TABLE prestamo (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  libro_id         INT NOT NULL,
  socio_id         INT NOT NULL,
  fecha_prestamo   DATE NOT NULL,
  fecha_limite     DATE NOT NULL,
  fecha_devolucion DATE NULL,
  estado           VARCHAR(12) NOT NULL DEFAULT 'ACTIVO',
  CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libro(id),
  CONSTRAINT fk_prestamo_socio FOREIGN KEY (socio_id) REFERENCES socio(id),
  CONSTRAINT ck_prestamo_estado CHECK (estado IN ('ACTIVO', 'DEVUELTO', 'VENCIDO')),
  CONSTRAINT ck_prestamo_fechas CHECK (fecha_limite >= fecha_prestamo)
);

CREATE INDEX idx_libro_titulo     ON libro(titulo);
CREATE INDEX idx_prestamo_socio   ON prestamo(socio_id, estado);

-- Datos de ejemplo para pruebas
INSERT INTO categoria (nombre, descripcion) VALUES
  ('Novela', 'Narrativa de ficción'),
  ('Ciencia', 'Divulgación científica'),
  ('Tecnología', 'Computación y programación');

INSERT INTO libro (titulo, autor, isbn, anio_publicacion, ejemplares_totales, ejemplares_disponibles, categoria_id) VALUES
  ('Cien años de soledad', 'Gabriel García Márquez', '9780307474728', 1967, 3, 3, 1),
  ('Breve historia del tiempo', 'Stephen Hawking', '9788498929478', 1988, 2, 2, 2),
  ('Clean Code', 'Robert C. Martin', '9780132350884', 2008, 2, 2, 3);

INSERT INTO socio (nombre, correo, telefono) VALUES
  ('Ana Pérez', 'ana@correo.com', '2281234567'),
  ('Luis Hernández', 'luis@correo.com', '2287654321');
