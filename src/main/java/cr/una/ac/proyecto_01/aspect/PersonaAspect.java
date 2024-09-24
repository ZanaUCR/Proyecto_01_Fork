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

public class PersonaAspect  {

    private static final Logger logger = LoggerFactory.getLogger(PersonaAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String LOG_FILE_PATH = "src/main/java/cr/una/ac/proyecto_01/logs/log.json";
    private final File logFile = new File(LOG_FILE_PATH);
    private boolean firstEntry = true;
    @Around("(@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)) "
            )
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        log.info("Starting execution method {}", methodName);

        Object result = joinPoint.proceed();

        log.info("Finished execution method {}", methodName);

        return result;
    }
    @Around("execution(* cr.una.ac.proyecto_01.controller.PersonaController.*(..))")
    public Object logRequestDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String threadName = Thread.currentThread().getName();
        Instant start = Instant.now();

        log.info("Request received at endpoint: {}, method: {}", endpoint, method);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("Exception in endpoint {}: {}", endpoint, e.getMessage());
            throw e;
        } finally {
            long duration = Instant.now().toEpochMilli() - start.toEpochMilli();
            int status = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse().getStatus();
            logRequestToFile(endpoint, method, duration, threadName, status);
        }
        return result;
    }
    private void logRequestToFile(String endpoint, String method, long duration, String threadName, int status) {
        LogEntry logEntry = new LogEntry(endpoint, method, "INFO", duration, threadName, "Request processed", Instant.now().toString(), status, null);
        writeLogToFile(logEntry);
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
