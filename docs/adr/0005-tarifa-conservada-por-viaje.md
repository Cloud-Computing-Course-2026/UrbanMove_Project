# ADR-0005: Conservar la tarifa al iniciar cada viaje

- Estado: Aceptado (implementado; registro retrospectivo)
- Fecha de registro: 2026-10-31
- Fecha de decisión original: No consta
- Alcance: Cálculo del coste preliminar

## Contexto

Cambiar la configuración de precios durante un viaje no debe modificar la tarifa que se aplicó al iniciarlo. El cálculo debe ser reproducible sin usar números monetarios de coma flotante.

## Decisión

Copiar al viaje el desbloqueo, precio por minuto y moneda EUR al iniciarlo. Obtener el tipo de vehículo del gateway y elegir la tarifa configurada. Calcular con `BigDecimal` y dos decimales: desbloqueo más minutos empezados, con un mínimo de un minuto. La duración expuesta se expresa en segundos enteros; el cálculo de minutos conserva la fracción temporal. Es un importe preliminar, sin cobro real.

## Alternativas

Contraste retrospectivo: leer la tarifa vigente al finalizar alteraría viajes ya iniciados; usar coma flotante introduciría errores de representación; delegar a facturación requiere un servicio que aún no está integrado.

## Consecuencias

El coste conserva la tarifa original incluso tras un cambio de configuración. La fila del viaje y los snapshots contienen esa información. No se cubren descuentos, impuestos, pagos, tarifas dinámicas ni conciliación con facturación; introducirlos requiere revisar el modelo y los contratos.

## Verificación y seguimiento

Evidencia: [RideService](../../services/rides/src/main/java/com/urbanmove/rides/service/RideService.java) y [modelo](../../services/rides/src/main/java/com/urbanmove/rides/model/Ride.java). Las [pruebas existentes](../../services/rides/src/test/java/com/urbanmove/rides/RidesIntegrationTest.java) incluyen conservación de tarifa, mínimo de un minuto y fronteras de fracciones de segundo. Revisar al integrar facturación o modificar reglas tarifarias.
