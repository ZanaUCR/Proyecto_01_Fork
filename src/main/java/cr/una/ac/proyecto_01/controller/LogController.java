package cr.una.ac.proyecto_01.controller;


import cr.una.ac.proyecto_01.entity.LogEntry;
import cr.una.ac.proyecto_01.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    LogService logService;


    // Endpoint para obtener todos los logs
    @GetMapping("/all")
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        Optional<List<LogEntry>> optionalLogs = logService.readLogs();

        // Devuelve los logs si están presentes
        // Devuelve 204 No Content si no hay logs
        return optionalLogs.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());

    }

    // Endpoint para el reporte de errores
    @GetMapping("/report/errors")
    public ResponseEntity<Map<String, Object>> getErrorReport() {
        Optional<Map<String, Object>> optionalErrorReport = logService.generateErrorReport();

        // Devuelve el reporte de errores si está disponible
        return optionalErrorReport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No logs available to generate error report")));

    }

    // Endpoint para el reporte de tiempos de respuesta
    @GetMapping("/report/response-times")
    public ResponseEntity<Map<String, Object>> getResponseTimeReport() {
        Optional<Map<String, Object>> optionalResponseTimeReport = logService.generateResponseTimeReport();

        // Devuelve el reporte de tiempos de respuesta si está disponible
        return optionalResponseTimeReport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No logs available to generate response time report")));

    }

    // Endpoint para el reporte de uso de endpoints
    @GetMapping("/report/usage")
    public ResponseEntity<Map<String, Object>> getEndpointUsageReport() {
        Optional<Map<String, Object>> optionalEndpointUsageReport = logService.generateEndpointUsageReport();

        // Devuelve el reporte de uso de endpoints si está disponible
        return optionalEndpointUsageReport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No logs available to generate endpoint usage report")));

    }

    // Endpoint para el reporte de eventos críticos
    @GetMapping("/report/critical-events")
    public ResponseEntity<Map<String, Object>> getCriticalEventReport() {
        Optional<Map<String, Object>> optionalCriticalEventReport = logService.generateCriticalEventReport();

        // Devuelve el reporte de eventos críticos si está disponible
        return optionalCriticalEventReport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No logs available to generate critical event report")));

    }

    // Endpoint para el reporte del estado de la aplicación
    @GetMapping("/report/application-status")
    public ResponseEntity<Map<String, Object>> getApplicationStatusReport() {
        Optional<Map<String, Object>> optionalApplicationStatusReport = logService.generateApplicationStatusReport();

        // Devuelve el reporte del estado de la aplicación si está disponible
        return optionalApplicationStatusReport.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No logs available to generate application status report")));

    }
}