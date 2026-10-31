import os
import json
import logging
import urllib.request
import urllib.error

# Configuración de Logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

RESERVATIONS_SERVICE_URL = os.environ.get(
    "RESERVATIONS_SERVICE_URL", "http://reservations-service:8082"
)
INTERNAL_AUTH_TOKEN = os.environ.get(
    "INTERNAL_AUTH_TOKEN", "urbanmove-internal-secret-token-2026"
)

def lambda_handler(event, context):
    """
    Función Serverless encargada de ejecutar periódicamente (AWS EventBridge cron)
    o por evento la expiración de reservas caducadas en el Microservicio de Reservas.
    
    Llama al endpoint interno de reservas liberando los vehículos cuyo TTL ha expirado.
    """
    logger.info("Iniciando escaneo de reservas expiradas.")
    target_url = f"{RESERVATIONS_SERVICE_URL}/api/v1/internal/reservations/expire-timed-out"

    req = urllib.request.Request(
        target_url,
        data=b"{}",
        headers={
            "Content-Type": "application/json",
            "X-Internal-Token": INTERNAL_AUTH_TOKEN
        },
        method="POST"
    )

    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            status_code = response.getcode()
            response_body = response.read().decode("utf-8")
            result = json.loads(response_body)
            logger.info("Resultado de expiración: %s (Status: %s)", result, status_code)
            return {
                "statusCode": status_code,
                "body": json.dumps({
                    "message": "Proceso de expiración ejecutado correctamente",
                    "data": result
                })
            }
    except urllib.error.HTTPError as e:
        error_msg = e.read().decode("utf-8")
        logger.error("Error HTTP al invocar expiración: %s - %s", e.code, error_msg)
        return {
            "statusCode": e.code,
            "body": json.dumps({"error": error_msg})
        }
    except Exception as e:
        logger.exception("Fallo inesperado al conectar con el microservicio de reservas: %s", str(e))
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }
