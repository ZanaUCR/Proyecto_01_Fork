package cr.una.ac.proyecto_01.aspect;


import com.fasterxml.jackson.databind.ObjectMapper;
import cr.una.ac.proyecto_01.entity.LogEntry;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Instant;


@Aspect
@Component
@Slf4j

public class PersonaAspect {

    private static final Logger logger = LoggerFactory.getLogger(PersonaAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String LOG_FILE_PATH = "src/main/java/cr/una/ac/proyecto_01/logs/log.json";
    private final File logFile = new File(LOG_FILE_PATH);
    private boolean firstEntry = true;



    // Interceptar métodos de los controladores específicos
    @Around("execution(* cr.una.ac.proyecto_01.controller.PersonaController.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // Obtener la solicitud y la respuesta actuales
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        long startTime = System.currentTimeMillis();
        String errorMessage = null;

        // Registrar el inicio de la ejecución del método
        log.info("Starting execution method {}", joinPoint.getSignature().getName());

        Object result;

        try {
            // Continuar con la ejecución del método
            result = joinPoint.proceed();
        } catch (Exception e) {
            response.setStatus(500); // En caso de excepción, establecer el estado HTTP como 500
            throw e;
        } finally {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            String threadName = Thread.currentThread().getName();
            int status = response.getStatus();

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

            log.info("Finished execution method {}", joinPoint.getSignature().getName());
        }

        return result;
    }

    private String getErrorMessageForStatus(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "HTTP Error " + status;
        };
    }

    private void writeLogToFile(LogEntry logEntry) {
        try (RandomAccessFile log = new RandomAccessFile(logFile, "rw")) {
            long length = logFile.length();

            if (!firstEntry) {
                log.seek(length - 2);
                log.write(",\n".getBytes());
            } else {
                log.seek(length - 1);
            }

            log.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(logEntry));
            log.write("\n]".getBytes());

            firstEntry = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
