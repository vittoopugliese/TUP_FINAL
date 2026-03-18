# Contrato de Tipos de Step y valueJson

Contrato único para serialización/deserialización entre Android Room, backend y documentación.

## Estados de Step

| Estado | Descripción |
|--------|-------------|
| PENDING | Sin valor o incompleto |
| COMPLETED | Valor válido (legacy SUCCESS se mapea aquí) |
| FAILED | Validación fallida |

## Estados de Test

| Estado | Descripción |
|--------|-------------|
| PENDING | Al menos un step aplicable sin completar |
| COMPLETED | Todos los steps aplicables válidos |
| FAILED | Al menos un step aplicable inválido |

## Tipos de Input (testStepType)

### BINARY
- UI: Dropdown Sí/No
- valueJson: `{"value": true|false|null, "valueType": "BOOLEAN_VALUE"}`

### DATE_RANGE
- UI: Dos date pickers (Desde / Hasta)
- valueJson: `{"from": "yyyy-MM-dd", "to": "yyyy-MM-dd", "valueType": "DATE_RANGE_VALUE"}`
- Validación: from <= to

### SIMPLE_VALUE
- UI: Texto, número o fecha según subtipo
- valueJson: `{"value": "...", "valueType": "STRING_VALUE"|"NUMERIC_VALUE"|"DATE_VALUE"|"BOOLEAN_VALUE"}`

### NUMERIC_RANGE
- UI: Input numérico + referencia min/max
- valueJson: `{"value": number, "valueType": "NUMERIC_VALUE"|"NUMERIC_UNIT_VALUE"}`
- minValue, maxValue en columnas de la tabla

### MULTI_VALUE
- UI: Varios subcampos
- valueJson: `{"values": [{"name":"...","value":"...","valueType":"..."}]}`

### RANGE (legacy)
- Mapear a NUMERIC_RANGE cuando minValue/maxValue son numéricos

## N/A por Step

- Persistir como `applicable=false`
- Excluir de validación y del resultado del test
- Deshabilitar inputs cuando N/A está activo

## Tipos de Observación (observations.type)

| Tipo | Descripción | Efecto en step/test |
|------|-------------|---------------------|
| REMARKS | Observación (texto, foto opcional) | No afecta estado |
| DEFICIENCIES | Deficiencia (texto + foto requerida) | Step y test pasan a FAILED |
| DEFICIENCY | Legacy singular | Mapear a DEFICIENCIES al leer |
