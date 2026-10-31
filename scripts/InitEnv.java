package scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class InitEnv {
    public static void main(String[] args) {
        File envFile = new File(".env");
        if (envFile.exists()) {
            System.out.println("[INFO] El archivo .env ya existe. No se sobrescribe.");
            return;
        }

        File exampleFile = new File(".env.example");
        if (exampleFile.exists()) {
            try {
                Files.copy(exampleFile.toPath(), envFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[SUCCESS] Archivo .env copiado con éxito desde .env.example.");
                return;
            } catch (IOException e) {
                System.err.println("[WARN] No se pudo copiar .env.example: " + e.getMessage());
            }
        }

        String template = """
                # ==============================================================================
                # URBANMOVE - Variables de Entorno Locales (.env)
                # ==============================================================================
                POSTGRES_USER=urbanmove
                POSTGRES_PASSWORD=urbanmove_secure_password
                POSTGRES_DB=postgres
                RABBITMQ_DEFAULT_USER=urbanmove
                RABBITMQ_DEFAULT_PASS=urbanmove_broker_pass
                MINIO_ROOT_USER=minioadmin
                MINIO_ROOT_PASSWORD=minioadminpassword
                URBANMOVE_INTERNAL_AUTH_TOKEN=urbanmove-internal-secret-token-2026
                """;

        try {
            Files.writeString(Path.of(".env"), template);
            System.out.println("[SUCCESS] Archivo .env generado exitosamente.");
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo generar el archivo .env: " + e.getMessage());
        }
    }
}
