package cr.una.ac.proyecto_01.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cr.una.ac.proyecto_01.entity.LogEntry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    File jsonFile = new File("src/main/java/cr/una/ac/proyecto_01/logs/log.json");
    // Lista que contiene todos los logs cargados al inicializar el servicio
    private List<LogEntry> logs;

    // Constructor del servicio donde cargamos los logs al iniciar
    public LogService() {
        try {
            this.logs = objectMapper.readValue(jsonFile, new TypeReference<List<LogEntry>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Metodo para obtener los logs de forma opcional
    public Optional<List<LogEntry>> readLogs() {
        return logs.isEmpty() ? Optional.empty() : Optional.of(logs);
    }

    // Reporte de Errores
    public Optional<Map<String, Object>> generateErrorReport() {
        if (logs.isEmpty()) {
            return Optional.empty();
        }


        // Número total de errores registrados por tipo
        Map<String, Long> errorCounts = logs.stream()
                .filter(log -> log.getStatus() >= 400)
                .collect(Collectors.groupingBy(LogEntry::getErrorMessage, Collectors.counting()));

        // Errores más frecuentes
        Optional<String> mostFrequentError = errorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);

        // Horas pico de errores
        Map<Integer, Long> errorPeakHours = logs.stream()
                .filter(log -> log.getStatus() >= 400)
                .collect(Collectors.groupingBy(log ->
                                LocalDateTime.parse(log.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME).getHour(),
                        Collectors.counting()
                ));

        Optional<Integer> peakHour = errorPeakHours.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);

        Map<String, Object> report = new HashMap<>();
        report.put("totalErrorsByType", errorCounts);
        report.put("mostFrequentError", mostFrequentError.orElse("No errors found")); //Optional
        report.put("errorPeakHours", errorPeakHours);
        report.put("peakHour", peakHour.orElse(null)); //Optional
        return Optional.of(report); // Devolver el reporte
    }




    // Reporte de Tiempos de Respuesta
    public Optional<Map<String, Object>> generateResponseTimeReport() {
        if (logs.isEmpty()) {
            return Optional.empty();
        }

        List<Long> responseTimes = logs.stream()
                .map(LogEntry::getResponseTimeMs)
                .sorted()
                .toList();

        long minTime = responseTimes.getFirst();
        long maxTime = responseTimes.getLast();
        double averageTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long medianTime = responseTimes.get(responseTimes.size() / 2);

        // Distribución por endpoint
        Map<String, Double> avgTimeByEndpoint = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getEndpoint, Collectors.averagingLong(LogEntry::getResponseTimeMs)));

        // Solicitudes lentas (outliers: tiempos mayores al percentil 90)
        double ninetyPercentile = responseTimes.get((int) (responseTimes.size() * 0.9));

        Map<String, Object> report = new HashMap<>();
        report.put("minResponseTime", minTime);
        report.put("maxResponseTime", maxTime);
        report.put("averageResponseTime", averageTime);
        report.put("medianResponseTime", medianTime);
        report.put("responseTimeDistributionByEndpoint", avgTimeByEndpoint);
        report.put("slowRequests", ninetyPercentile);
        return Optional.of(report); // Devolver el reporte
    }





    // Reporte de Uso de Endpoints
    public Optional<Map<String, Object>> generateEndpointUsageReport() {
        if (logs.isEmpty()) {
            return Optional.empty();
        }

        // Conteo por endpoint y metodo
        Map<String, Map<String, Long>> usageByEndpointAndMethod = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::getEndpoint,
                        Collectors.groupingBy(LogEntry::getMethod, Collectors.counting())));

        // Endpoints más utilizados
        Optional<String> mostUsedEndpoint = usageByEndpointAndMethod.entrySet().stream()
                .max(Comparator.comparingLong(entry -> entry.getValue().values().stream().mapToLong(Long::longValue).sum()))
                .map(Map.Entry::getKey);

        // Endpoints menos utilizados
        Optional<String> leastUsedEndpoint = usageByEndpointAndMethod.entrySet().stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().values().stream().mapToLong(Long::longValue).sum()))
                .map(Map.Entry::getKey);

        Map<String, Object> report = new HashMap<>();
        report.put("usageByEndpointAndMethod", usageByEndpointAndMethod);
        report.put("mostUsedEndpoint", mostUsedEndpoint.orElse("No data"));
        report.put("leastUsedEndpoint", leastUsedEndpoint.orElse("No data"));
        return Optional.of(report);
    }




    // Reporte de Eventos Críticos
    public Optional<Map<String, Object>> generateCriticalEventReport() {
        if (logs.isEmpty()) {
            return Optional.empty();
        }

        // Filtrar eventos críticos (status >= 500 o mensajes con "critical")
        List<LogEntry> criticalEvents = logs.stream()
                .filter(log -> log.getStatus() >= 500 || log.getMessage().toLowerCase().contains("critical"))
                .collect(Collectors.toList());

        // Conteo de eventos críticos
        long criticalEventCount = criticalEvents.size();

        Map<String, Object> report = new HashMap<>();
        report.put("criticalEvents", criticalEvents);
        report.put("criticalEventCount", criticalEventCount);
        return Optional.of(report);
    }





    // Reporte de Estado de la Aplicación
    public Optional<Map<String, Object>> generateApplicationStatusReport() {
        if (logs.isEmpty()) {
            return Optional.empty();
        }

        long totalRequests = logs.size();
        long totalErrors = logs.stream().filter(log -> log.getStatus() >= 400).count();
        double averageResponseTime = logs.stream().mapToLong(LogEntry::getResponseTimeMs).average().orElse(0);

        Map<String, Object> report = new HashMap<>();
        report.put("totalRequests", totalRequests);
        report.put("totalErrors", totalErrors);
        report.put("averageResponseTime", averageResponseTime);
        return Optional.of(report); // Devolver el reporte
    }


}