package cr.una.ac.proyecto_01.aspect;


import com.fasterxml.jackson.databind.ObjectMapper;
import cr.una.ac.proyecto_01.entity.LogEntry;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Instant;


@Aspect
@Component
public class PersonaAspect implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PersonaAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String LOG_FILE_PATH = "src/main/java/cr/una/ac/proyecto_01/logs/log.json";
    private final File logFile = new File(LOG_FILE_PATH);
    private boolean firstEntry = true;



    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String errorMessage = null;

        // Filtrar solo las peticiones que van al controlador "persona"
        String requestURI = httpRequest.getRequestURI();
        if (!requestURI.startsWith("/api/persona")) {
            chain.doFilter(request, response);
            return;
        }

        if (firstEntry) {
            // Inicializar el archivo JSON si es la primera entrada
            try (FileWriter fileWriter = new FileWriter(logFile, false)) {
                fileWriter.write("[\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // Continuar con la cadena de filtros y controlar la respuesta
            chain.doFilter(request, response);
        }catch (Exception e) {
            System.out.println(httpResponse.getStatus());
            httpResponse.setStatus(500); // Asegurarse de que el estado sea 500 en caso de excepción

        } finally {
            // Capturar datos después de procesar la petición
            long responseTimeMs = System.currentTimeMillis() - startTime;
            String endpoint = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            String threadName = Thread.currentThread().getName();
            int status = httpResponse.getStatus();

            // Generar un mensaje de error más específico basado en el código de estado HTTP
            if (status >= 400) {
                errorMessage = getErrorMessageForStatus(status);
            }


            // Crear una entrada de log
            LogEntry logEntry = new LogEntry(
                    endpoint,
                    method,
                    status >= 400 ? "ERROR" : "INFO",
                    responseTimeMs,
                    threadName,
                    status >= 400 ? "Error occurred" : "Request processed",
                    Instant.now().toString(),
                    status,
                    errorMessage
            );

            // Guardar la entrada de log en el archivo JSON
            writeLogToFile(logEntry);
        }


    }

    private String getErrorMessageForStatus(int status) {
        // Devuelve mensajes personalizados basados en el código de estado HTTP
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 408 -> "Request Timeout";
            case 413 -> "Payload Too Large";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "HTTP Error " + status;
        };
    }
    private void writeLogToFile(LogEntry logEntry) {
        try (RandomAccessFile log = new RandomAccessFile(logFile, "rw")) {
            long length = logFile.length();

            // Si el archivo tiene contenido, moverse antes del último corchete ]
            if (!firstEntry) {
                log.seek(length - 2);  // Antes de `]`
                log.write(",\n".getBytes());  // Añadir coma antes de la nueva entrada
            } else {
                log.seek(length - 1);  // Si es la primera entrada, antes del `]`
            }

            // Escribir el nuevo log en formato JSON
            log.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(logEntry));

            // Añadir el corchete de cierre `]`
            log.write("\n]".getBytes());

            firstEntry = false;  // A partir de ahora, ya no es la primera entrada
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
