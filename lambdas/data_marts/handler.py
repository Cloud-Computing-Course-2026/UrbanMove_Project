import os
import json
import logging
import boto3

logger = logging.getLogger()
logger.setLevel(logging.INFO)

S3_BUCKET_NAME = os.environ.get("DATA_LAKE_BUCKET", "urbanmove-data-lake")
S3_ENDPOINT_URL = os.environ.get("S3_ENDPOINT_URL", None)

s3_client = boto3.client("s3", endpoint_url=S3_ENDPOINT_URL)

def lambda_handler(event, context):
    """
    Función analítica serverless que procesa datos de la capa 'raw/' o 'trusted/'
    hacia la capa 'curated/' (Data Marts), calculando KPIs como tasa de cancelación,
    tiempo medio de reserva antes de expiración o utilización de flota.
    """
    logger.info("Iniciando agregación de métricas para Data Marts en capa curated/")
    
    # Simulación de generación de reporte / agregado analítico
    curated_key = "curated/reservations_summary/daily_kpi_latest.json"
    kpi_payload = {
        "report_name": "Reservations Operational Metrics",
        "processed_by": "data_marts_lambda",
        "layers": ["raw", "trusted", "curated"]
    }

    s3_client.put_object(
        Bucket=S3_BUCKET_NAME,
        Key=curated_key,
        Body=json.dumps(kpi_payload, indent=2).encode("utf-8"),
        ContentType="application/json"
    )

    return {
        "statusCode": 200,
        "body": json.dumps({"status": "Curated data mart generated", "key": curated_key})
    }
