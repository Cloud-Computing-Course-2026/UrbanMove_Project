import os
import json
import logging
from datetime import datetime, timezone
import boto3

logger = logging.getLogger()
logger.setLevel(logging.INFO)

S3_BUCKET_NAME = os.environ.get("DATA_LAKE_BUCKET", "urbanmove-data-lake")
S3_ENDPOINT_URL = os.environ.get("S3_ENDPOINT_URL", None) # MinIO para local

s3_client = boto3.client("s3", endpoint_url=S3_ENDPOINT_URL)

def lambda_handler(event, context):
    """
    Ingiere eventos de dominio emitidos por el bus de mensajería (RabbitMQ/SQS)
    y los almacena de forma inmutable en la capa 'raw/' del Data Lake en Amazon S3.
    Particionado por dominio, año, mes, día y hora.
    """
    records = event.get("Records", [])
    logger.info("Recibidos %d registros para ingesta en Data Lake raw", len(records))

    now = datetime.now(timezone.utc)
    ingested_keys = []

    for record in records:
        try:
            body_str = record.get("body", "{}")
            payload = json.loads(body_str) if isinstance(body_str, str) else record

            event_id = payload.get("eventId", f"evt-{int(now.timestamp())}")
            event_type = payload.get("eventType", "UnknownEvent")
            
            # Determinamos el subdominio según el tipo de evento
            domain = "reservations" if "Reservation" in event_type or "Vehicle" in event_type else "general"

            s3_key = (
                f"raw/domain={domain}/year={now.year}/month={now.strftime('%m')}/"
                f"day={now.strftime('%d')}/{event_type}_{event_id}.json"
            )

            s3_client.put_object(
                Bucket=S3_BUCKET_NAME,
                Key=s3_key,
                Body=json.dumps(payload, indent=2).encode("utf-8"),
                ContentType="application/json"
            )
            ingested_keys.append(s3_key)
            logger.info("Evento almacenado con éxito en S3: %s", s3_key)

        except Exception as e:
            logger.exception("Error ingiriendo registro: %s", str(e))

    return {
        "statusCode": 200,
        "body": json.dumps({
            "message": "Ingesta en capa raw completada",
            "ingested_count": len(ingested_keys),
            "keys": ingested_keys
        })
    }
