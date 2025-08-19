package br.com.btihelpbot.bti_api.controller;

import br.com.btihelpbot.bti_api.dto.CommandLogDTO;
import br.com.btihelpbot.bti_api.model.CommandLog;
import br.com.btihelpbot.bti_api.service.MetricsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService){
        this.metricsService = metricsService;
    }

    @GetMapping("/user-command-count")
    public ResponseEntity<Long> getUserCommandCount(@RequestParam String userId) {
        Long count = metricsService.countCommandsByUserId(userId);
        return ResponseEntity.ok(count);
    }

    
    @GetMapping()
    public ResponseEntity<Page<CommandLog>> findLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(required = false) String chatType,
            @RequestParam(required = false) List<String> commands,
            Pageable pageable
            ){
        Page<CommandLog> results = metricsService.findLogsByCriteria(startDate, endDate, chatType, commands, pageable);

        return ResponseEntity.ok(results);
    }


    @PostMapping("/command")
    public ResponseEntity<Void> logCommand(@RequestBody CommandLogDTO commandLogDTO){
        metricsService.saveLog(commandLogDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
