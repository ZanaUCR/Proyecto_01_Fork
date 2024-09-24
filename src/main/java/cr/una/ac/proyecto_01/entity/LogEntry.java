package cr.una.ac.proyecto_01.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class LogEntry {
    private String endpoint;
    private String method;
    private String level;
    private long responseTimeMs;
    private String thread;
    private String message;
    private String timestamp;
    private int status;
    private String errorMessage;

}