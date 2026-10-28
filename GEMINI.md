# Directrices Arquitectónicas del Backend - Proyecto SYNAGON

## 1. Regla de Oro de Nomenclatura e Idioma
- **ABSOLUTAMENTE TODO el código debe estar en ESPAÑOL**: nombres de paquetes, clases, interfaces, métodos, variables, propiedades, tablas SQL, columnas y endpoints REST.
- Prohibido mezclar spanglish (Mal: `getEnfrentamientoById`, Bien: `obtenerEnfrentamientoPorId`).

## 2. Arquitectura Hexagonal (Puertos y Adaptadores)
Estructura obligatoria bajo `src/main/kotlin/com/synagon/api`:
- `dominio/`
    - `modelos/`: Clases de dominio puras e inmutables (sin anotaciones de Spring ni JPA). Contienen las reglas de negocio y validaciones de estado.
    - `puertos/entrada/`: Interfaces de casos de uso (ej. `ProponerFechasJornadaUseCase`).
    - `puertos/salida/`: Interfaces de persistencia y notificaciones (ej. `RepositorioEnfrentamientoPuerto`).
    - `excepciones/`: Excepciones de negocio (`ConvocatoriaInvalidaExcepcion`, `ResultadoPadelIlegalExcepcion`).
- `aplicacion/`
    - `servicios/`: Implementación de los casos de uso (`@Service` y `@Transactional`). Orquestan el dominio.
- `infraestructura/`
    - `adaptadores/entrada/rest/`: Controladores (`@RestController`), DTOs de petición/respuesta (`@Valid`) y `ManejadorGlobalExcepciones` (`@RestControllerAdvice`).
    - `adaptadores/salida/persistencia/`: Entidades JPA (`EntidadEnfrentamiento`), interfaces Spring Data (`RepositorioJpaEnfrentamiento`) y Mappers de persistencia.

## 3. Reglas de Negocio Inquebrantables de Synagon
1. **Jerarquía Escalable:** Toda entidad pertenece a `Liga -> Temporada -> Division -> Jornada -> Enfrentamiento`.
2. **Negociación de Fechas:** El capitán local ofrece exactamente 2 `PropuestaFecha`. Los jugadores votan su disponibilidad (`DISPONIBLE`, `NO_DISPONIBLE`, `DUDA`) por cada fecha propuesta.
3. **Convocatoria y Alineación:** Una vez fijada la `fechaDefinitiva`, los jugadores confirman disponibilidad final. El capitán convoca exactamente a 6 jugadores y los asigna en 3 `PartidoPareja` (Pareja 1, Pareja 2, Pareja 3). Un jugador NO puede repetirse en dos parejas de la misma jornada.
4. **Actas y Tanteo de Pádel:** Un partido se juega al mejor de 3 sets. Valida que los sets tengan tanteos legales de pádel (6-0 a 6-4, 7-5 o 7-6). Al confirmar el acta, se actualizan atómicamente las estadísticas de los 6 jugadores y la clasificación de la división.

## 4. Base de Datos y Rendimiento
- Todas las claves primarias deben ser `UUID`.
- Usa `Flyway` en `src/main/resources/db/migration/` para cualquier cambio de esquema.
- Añade índices explícitos en las claves foráneas de consulta frecuente (`enfrentamiento_id`, `equipo_id`, `jugador_id`).