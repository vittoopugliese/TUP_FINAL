# Docs - Documentación del Proyecto

Documentación completa del sistema Inspections (Sistema de Inspección Digital de Dispositivos contra Incendios).

## Archivos a Crear (según alcance)

| Documento | Descripción | Audiencia |
|-----------|-------------|-----------|
| `manual-usuario-inspector.pdf` | Manual de uso para inspectores | Inspectores |
| `manual-usuario-operador.pdf` | Manual de uso para operadores | Operadores |
| `manual-administracion.pdf` | Manual de administración del sistema | Administradores |
| `arquitectura-tecnica.md` | Documentación técnica de arquitectura | Desarrolladores |
| `guia-plantillas-test.md` | Guía de configuración de plantillas de test | Configuradores |
| `plan-pruebas.md` | Plan de pruebas y casos de test ejecutados | QA / Desarrolladores |
| `cumplimiento-normativo.md` | Documento de cumplimiento normativo | Auditoría |
| `diagrams/` | Diagramas de arquitectura, flujos, modelo de datos | Todos |

## Contenido Previsto por Documento

### Manual de usuario (Inspector)
- Login y recuperación de contraseña
- Listado y filtrado de inspecciones asignadas
- Iniciar/continuar inspección
- Asignar y remover miembros del equipo
- Navegación jerárquica (Locations → Zones → Devices)
- Ejecutar tests y completar steps
- Agregar observaciones y deficiencias con fotos
- Firmar inspección digitalmente
- Generar y descargar reporte PDF

### Manual de usuario (Operador)
- Login
- Listado de inspecciones asignadas
- Ejecutar tests y completar steps (sin firmar)
- Agregar observaciones y deficiencias
- Modo offline (no puede modificar equipo)

### Manual de administración
- Gestión de edificios/plantas
- Gestión de usuarios e inspectores
- Configuración de templates de tests
- Administración de tipos de dispositivos
- Visualización de inspecciones históricas

### Documentación técnica de arquitectura
- Diagrama de componentes (Android, Backend, DB)
- Arquitectura MVVM + Clean Architecture en Android
- Flujo de datos offline-first
- API REST (endpoints, autenticación)
- Modelo de datos y relaciones

### Guía de plantillas de test
- Tipos de input: Binary, Date Range, Simple Value, Numeric Range, Multi-value
- Cómo configurar un test por tipo de dispositivo
- Asignación automática de tests según tipo de device

### Plan de pruebas
- Casos de test por módulo
- Pruebas de integración
- Pruebas de sincronización offline/online
- Resultados de ejecución

## Estructura Prevista

```
docs/
├── README.md
├── manual-usuario-inspector.pdf
├── manual-usuario-operador.pdf
├── manual-administracion.pdf
├── arquitectura-tecnica.md
├── guia-plantillas-test.md
├── plan-pruebas.md
├── cumplimiento-normativo.md
└── diagrams/
    ├── arquitectura-general.png
    ├── flujo-autenticacion.png
    ├── flujo-inspeccion.png
    └── modelo-datos.png
```

## Entregables del Proyecto (según requisitos)

- Documentación técnica completa
- Manuales de usuario (inspector y operador)
- Presentación del proyecto
- Videos demostrativos de uso