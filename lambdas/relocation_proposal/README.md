# Lambda: Propuesta de Recolocación (`relocation_proposal`)

Función serverless encargada de:
- Analizar el balance de oferta y demanda por zona desde el Data Mart de demanda.
- Si una zona tiene menos de 3 recursos disponibles y más de 10 señales de demanda, busca zonas con exceso del mismo tipo (>5) y genera una orden de recolocación justificada en `relocations`.
