package scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class InitEnv {
    public static void main(String[] args) {
        File envFile = new File(".env");
        if (envFile.exists()) {
            System.out.println("[INFO] El archivo .env ya existe. No se sobrescribe.");
            return;
        }

        String template = """
                # =====================================================================
                # URBANMOVE - Variables de Entorno Locales (No versionar con datos reales)
                # =====================================================================
                POSTGRES_USER=urbanmove
                POSTGRES_PASSWORD=urbanmove_secure_password
                RABBITMQ_USER=urbanmove
                RABBITMQ_PASS=urbanmove_broker_pass
                MINIO_ROOT_USER=minioadmin
                MINIO_ROOT_PASSWORD=minioadminpassword
                INTERNAL_AUTH_TOKEN=urbanmove-internal-secret-token-2026
                """;

        try (FileWriter writer = new FileWriter(envFile)) {
            writer.write(template);
            System.out.println("[SUCCESS] Archivo .env generado exitosamente desde scripts/InitEnv.java.");
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo generar el archivo .env: " + e.getMessage());
        }
    }
}
