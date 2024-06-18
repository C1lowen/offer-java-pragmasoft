package com.offer.java.offer.controller;

import com.offer.java.offer.dto.*;
import com.offer.java.offer.service.ScriptExecutionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scripts")
@AllArgsConstructor
public class ScriptController {
    private final ScriptExecutionService scriptExecutionService;

    private final static String MESSAGE_STOPPED_SCRIPT = "Script stopped";

    private final static String MESSAGE_DELETE_SCRIPT = "Script deleted";

    //Evaluate arbitrary JavaScript code
    @PostMapping("/execute")
    public ScriptResult executeScript(@RequestBody String script, @RequestParam boolean blocking) {

        return scriptExecutionService.executeScript(script, blocking);
    }

    //Review the list of scripts
    @GetMapping
    public List<ScriptInfoShort> listScripts(
            @RequestParam(required = false) Optional<SortedByTime> sortedTime,
            @RequestParam(required = false) Optional<SortedByStatus> sortedStatus
    ) {
        SortedByTime sortTime = sortedTime.orElse(SortedByTime.DEFAULT);
        SortedByStatus sortStatus = sortedStatus.orElse(SortedByStatus.ALL);

        return scriptExecutionService.getAllScripts(sortTime, sortStatus);
    }

    //Get detailed script info
    @GetMapping("/{id}")
    public ScriptInfoResponse getScriptInfo(@PathVariable String id) {
        return scriptExecutionService.getInfoScriptById(id);
    }

    //Forcibly stop any running or scheduled script
    @DeleteMapping("/{id}/stop")
    public ResponseEntity<ScriptResponse> stopScript(@PathVariable String id) {
        scriptExecutionService.stopScript(id);
        return ResponseEntity.ok(ScriptResponse.builder().answer(MESSAGE_STOPPED_SCRIPT).build());
    }

    //Remove inactive scripts
    @DeleteMapping("/{id}")
    public ResponseEntity<ScriptResponse> removeScript(@PathVariable String id) {
        scriptExecutionService.deleteScript(id);
        return ResponseEntity.ok(ScriptResponse.builder().answer(MESSAGE_DELETE_SCRIPT).build());
    }
}
