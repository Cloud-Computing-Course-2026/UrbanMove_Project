# ADR-001: Adopción de Monorepo Híbrido y Arquitectura Orientada a Eventos

## Estado
Aceptado

## Contexto
El sistema requiere alta consistencia en operaciones transaccionales de movilidad y análisis escalable desacoplado.

## Decisión
Se adopta un monorepo híbrido con backend transaccional en Java 17 + Maven y procesamiento serverless/analítico en Python 3 + Boto3 / Lambdas.
La integración se realiza mediante publicación de eventos de dominio a RabbitMQ / SQS.

## Consecuencias
- Desacoplamiento total entre dominios.
- Escalabilidad independiente de componentes de alta carga.
