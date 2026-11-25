package com.bcsystems.siga.service.impl;

import com.bcsystems.siga.domain.*;
import com.bcsystems.siga.domain.en.AccionAuditoria;
import com.bcsystems.siga.domain.en.EstadoPersonalizacion;
import com.bcsystems.siga.dto.PersonalizacionRequest;
import com.bcsystems.siga.dto.PersonalizacionResponse;
import com.bcsystems.siga.dto.PersonalizacionVistaRequest;
import com.bcsystems.siga.dto.PersonalizacionVistaResponse;
import com.bcsystems.siga.exception.InvalidEntryException;
import com.bcsystems.siga.exception.NotFoundException;
import com.bcsystems.siga.repository.ClienteRepository;
import com.bcsystems.siga.repository.PersonaRepository;
import com.bcsystems.siga.repository.PersonalizacionRepository;
import com.bcsystems.siga.repository.ProductoMultimediaRepository;
import com.bcsystems.siga.repository.ProductoRepository;
import com.bcsystems.siga.service.AuditoriaService;
import com.bcsystems.siga.service.PersonalizacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersonalizacionServiceImpl implements PersonalizacionService {

    private static final Set<String> REQUIRED_VIEWS = Set.of(
            "FRENTE", "LATERAL_DERECHO", "ATRAS", "LATERAL_IZQUIERDO");

    private final PersonalizacionRepository personalizacionRepository;
    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository multimediaRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;

    @Value("${app.upload.dir:./uploads/multimedia}")
    private String uploadDir;

    public PersonalizacionServiceImpl(PersonalizacionRepository personalizacionRepository,
                                      ProductoRepository productoRepository,
                                      ProductoMultimediaRepository multimediaRepository,
                                      ClienteRepository clienteRepository,
                                      PersonaRepository personaRepository,
                                      AuditoriaService auditoriaService) {
        this.personalizacionRepository = personalizacionRepository;
        this.productoRepository = productoRepository;
        this.multimediaRepository = multimediaRepository;
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional
    public PersonalizacionResponse crear(PersonalizacionRequest request) {
        Producto producto = productoRepository.findById(request.idProducto())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new InvalidEntryException("El producto está inactivo");
        }
        if (!Boolean.TRUE.equals(producto.getEsPersonalizable())) {
            throw new InvalidEntryException("El producto no está configurado como termo personalizable");
        }
        if (request.colorHex() == null || !request.colorHex().matches("#[0-9a-fA-F]{6}")) {
            throw new InvalidEntryException("El color debe ser un valor HEX válido");
        }
        boolean colorConfigurado = producto.getColores().stream()
                .anyMatch(color -> Boolean.TRUE.equals(color.getActivo())
                        && color.getHex().equalsIgnoreCase(request.colorHex()));
        if (!colorConfigurado) {
            throw new InvalidEntryException("El color seleccionado no está configurado para este producto");
        }
        ProductoMultimedia modelo = multimediaRepository
                .findFirstByProductoIdProductoAndTipoOrderByOrdenAscIdMultimediaAsc(producto.getIdProducto(), "MODELO_3D")
                .orElseThrow(() -> new InvalidEntryException("El producto debe tener un archivo GLB de modelo 3D"));
        if (request.fechaEntrega().isBefore(LocalDate.now())) {
            throw new InvalidEntryException("La fecha de entrega no puede estar en el pasado");
        }

        Map<String, PersonalizacionVistaRequest> vistas = validarVistas(request);
        Cliente cliente = request.idCliente() != null
                ? clienteRepository.findById(request.idCliente()).orElseThrow(() -> new NotFoundException("Cliente no encontrado"))
                : null;
        Persona usuario = obtenerPersonaActual();

        Personalizacion personalizacion = Personalizacion.builder()
                .folio(generarFolio())
                .producto(producto)
                .cliente(cliente)
                .usuario(usuario)
                .estado(EstadoPersonalizacion.LISTA_PARA_COBRO)
                .skuProducto(producto.getSku())
                .nombreProducto(producto.getNombre())
                .modelo3dUrl(modelo.getUrl())
                .colorNombre(request.colorNombre())
                .colorHex(request.colorHex().toUpperCase(Locale.ROOT))
                .capasJson(request.capasJson())
                .precioUnitario(request.precioUnitario())
                .cantidad(request.cantidad())
                .fechaEntrega(request.fechaEntrega())
                .nota(request.nota())
                .build();

        personalizacion = personalizacionRepository.save(personalizacion);
        try {
            int orden = 0;
            for (var entry : vistas.entrySet()) {
                String url = guardarVista(personalizacion, entry.getKey(), entry.getValue().imagen());
                personalizacion.getVistas().add(PersonalizacionVista.builder()
                        .personalizacion(personalizacion)
                        .tipo(entry.getKey())
                        .url(url)
                        .orden(orden++)
                        .build());
            }
            personalizacion = personalizacionRepository.save(personalizacion);
        } catch (RuntimeException ex) {
            eliminarDirectorioPersonalizacion(personalizacion.getIdPersonalizacion());
            throw ex;
        }

        auditoriaService.registrar("PERSONALIZACION", personalizacion.getIdPersonalizacion(),
                AccionAuditoria.CREACION.name(), usuario.getUsuario(),
                "Se guardó la personalización " + personalizacion.getFolio());
        return toResponse(personalizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalizacionResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalizacionResponse> listar(String estado) {
        List<Personalizacion> items;
        if (estado == null || estado.isBlank()) {
            items = personalizacionRepository.findAllByOrderByFechaCreacionDesc();
        } else {
            try {
                items = personalizacionRepository.findByEstadoInOrderByFechaCreacionDesc(
                        List.of(EstadoPersonalizacion.valueOf(estado.toUpperCase(Locale.ROOT))));
            } catch (IllegalArgumentException ex) {
                throw new InvalidEntryException("Estado de personalización inválido");
            }
        }
        return items.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalizacionResponse> listarDisponibles() {
        return personalizacionRepository.findByEstadoInOrderByFechaCreacionDesc(
                        List.of(EstadoPersonalizacion.LISTA_PARA_COBRO, EstadoPersonalizacion.EN_POS))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public PersonalizacionResponse enviarAPos(Integer id) {
        Personalizacion personalizacion = buscar(id);
        if (personalizacion.getEstado() == EstadoPersonalizacion.VENDIDA
                || personalizacion.getEstado() == EstadoPersonalizacion.CANCELADA) {
            throw new InvalidEntryException("La personalización ya no está disponible para el POS");
        }
        personalizacion.setEstado(EstadoPersonalizacion.EN_POS);
        personalizacion = personalizacionRepository.save(personalizacion);
        return toResponse(personalizacion);
    }

    @Override
    @Transactional
    public Personalizacion obtenerParaVenta(Integer id) {
        Personalizacion personalizacion = buscar(id);
        if (personalizacion.getEstado() != EstadoPersonalizacion.LISTA_PARA_COBRO
                && personalizacion.getEstado() != EstadoPersonalizacion.EN_POS) {
            throw new InvalidEntryException("La personalización no está disponible para venta");
        }
        return personalizacion;
    }

    @Override
    @Transactional
    public void marcarVendida(Personalizacion personalizacion) {
        personalizacion.setEstado(EstadoPersonalizacion.VENDIDA);
        personalizacionRepository.save(personalizacion);
    }

    @Override
    @Transactional
    public void marcarDisponible(Personalizacion personalizacion) {
        if (personalizacion == null || personalizacion.getEstado() == EstadoPersonalizacion.CANCELADA) return;
        personalizacion.setEstado(EstadoPersonalizacion.LISTA_PARA_COBRO);
        personalizacionRepository.save(personalizacion);
    }

    private Personalizacion buscar(Integer id) {
        return personalizacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Personalización no encontrada"));
    }

    private Map<String, PersonalizacionVistaRequest> validarVistas(PersonalizacionRequest request) {
        Map<String, PersonalizacionVistaRequest> vistas = new LinkedHashMap<>();
        for (PersonalizacionVistaRequest vista : request.vistas()) {
            String tipo = vista.tipo().trim().toUpperCase(Locale.ROOT);
            if (!REQUIRED_VIEWS.contains(tipo) || vistas.put(tipo, vista) != null) {
                throw new InvalidEntryException("Las vistas deben ser frente, lateral derecho, atrás y lateral izquierdo");
            }
            if (!vista.imagen().startsWith("data:image/")) {
                throw new InvalidEntryException("Cada vista debe ser una imagen válida");
            }
        }
        if (!vistas.keySet().equals(REQUIRED_VIEWS)) {
            throw new InvalidEntryException("Debes enviar las cuatro vistas del producto");
        }
        return vistas;
    }

    private String guardarVista(Personalizacion personalizacion, String tipo, String dataUrl) {
        try {
            int separator = dataUrl.indexOf(',');
            if (separator < 0) throw new InvalidEntryException("Imagen de vista inválida");
            byte[] bytes = Base64.getDecoder().decode(dataUrl.substring(separator + 1));
            if (bytes.length == 0 || bytes.length > 10 * 1024 * 1024) {
                throw new InvalidEntryException("Cada vista debe pesar menos de 10 MB");
            }
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve("personalizaciones")
                    .resolve(String.valueOf(personalizacion.getIdPersonalizacion()));
            Files.createDirectories(base);
            String fileName = tipo.toLowerCase(Locale.ROOT) + ".png";
            Files.write(base.resolve(fileName), bytes);
            return "/uploads/personalizaciones/" + personalizacion.getIdPersonalizacion() + "/" + fileName;
        } catch (IllegalArgumentException ex) {
            throw new InvalidEntryException("Una de las vistas no contiene una imagen válida");
        } catch (IOException ex) {
            throw new InvalidEntryException("No se pudo guardar una vista de la personalización");
        }
    }

    private String generarFolio() {
        String folio;
        do {
            folio = "PERS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        } while (personalizacionRepository.existsByFolio(folio));
        return folio;
    }

    private Persona obtenerPersonaActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return personaRepository.findByUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private void eliminarDirectorioPersonalizacion(Integer id) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve("personalizaciones").resolve(String.valueOf(id));
            if (Files.exists(dir)) {
                try (var files = Files.walk(dir)) {
                    files.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
                    });
                }
            }
        } catch (IOException ignored) { }
    }

    private PersonalizacionResponse toResponse(Personalizacion p) {
        List<PersonalizacionVistaResponse> vistas = p.getVistas().stream()
                .map(v -> new PersonalizacionVistaResponse(v.getIdVista(), v.getTipo(), v.getUrl(), v.getOrden()))
                .collect(Collectors.toList());
        String clienteNombre = p.getCliente() == null ? null
                : (p.getCliente().getNombre() + " " + (p.getCliente().getApellidoPaterno() == null ? "" : p.getCliente().getApellidoPaterno())).trim();
        return new PersonalizacionResponse(
                p.getIdPersonalizacion(), p.getFolio(), p.getProducto().getIdProducto(),
                p.getSkuProducto(), p.getNombreProducto(),
                p.getCliente() != null ? p.getCliente().getIdCliente() : null,
                clienteNombre, p.getEstado().name(), p.getModelo3dUrl(), p.getColorNombre(), p.getColorHex(),
                p.getCapasJson(), p.getPrecioUnitario(), p.getCantidad(), p.getFechaEntrega(), p.getNota(),
                p.getFechaCreacion(), p.getFechaActualizacion(), vistas);
    }
}
