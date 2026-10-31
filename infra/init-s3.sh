#!/bin/bash
# ==============================================================================
# URBANMOVE - Inicialización de S3 Data Lake en LocalStack
# Capas requeridas por AGENTS.md: raw/, trusted/, curated/
# ==============================================================================
echo "[INFO] Inicializando Data Lake en S3 (LocalStack)..."

awslocal s3 mb s3://urbanmove-lake || true
awslocal s3api put-object --bucket urbanmove-lake --key raw/
awslocal s3api put-object --bucket urbanmove-lake --key trusted/
awslocal s3api put-object --bucket urbanmove-lake --key curated/

echo "[SUCCESS] Data Lake urbanmove-lake configurado con capas raw/, trusted/ y curated/."
