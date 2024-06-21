package com.offer.java.offer.service;

import com.offer.java.offer.dto.*;
import com.offer.java.offer.exception.ApplicationException;
import com.offer.java.offer.exception.NotFoundException;
import com.offer.java.offer.exception.ScriptRunException;
import com.offer.java.offer.mapper.MapperScript;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static com.offer.java.offer.dto.Status.*;

@Service
public class ScriptExecutionService {

    private final ConcurrentHashMap<String, ScriptInfo> scriptStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ByteArrayOutputStream> outputStorage = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final String MESSAGE_SCRIPT_NOT_FOUND = "Script with this id not found";
    private static final String MESSAGE_SCRIPT_RUNNING = "The script is currently running and cannot be deleted";
    private static final String MESSAGE_SCRIPT_NOT_RUNNING = "Script with is not running";

    public ScriptResult executeScript(String script, boolean isBlocking) {
        String scriptId = UUID.randomUUID().toString();
        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptStorage.put(scriptId, scriptInfo);

        Callable<ScriptResult> task = createScriptTask(script, scriptInfo);

        if (isBlocking) {
            try {
                return task.call();
            } catch (Exception e) {
                ScriptResult result = new ScriptResult();
                result.setError(e.getMessage());
                result.setStatus(ERROR);
                return result;
            }
        } else {
            Future<?> future = executor.submit(task);
            scriptInfo.setFuture(future);
            return new ScriptResult(scriptId);
        }
    }


    private Callable<ScriptResult> createScriptTask(String script, ScriptInfo scriptInfo) {
        return () -> {
            scriptInfo.getResult().setStatus(PROCESSING);

            ScriptResult result = new ScriptResult();
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            ByteArrayOutputStream errContent = new ByteArrayOutputStream();

            outputStorage.put(scriptInfo.getId(), outContent);

            try (PrintStream stdout = new PrintStream(outContent);
                 PrintStream stderr = new PrintStream(errContent);
                 Context context = Context.newBuilder("js")
                         .out(stdout)
                         .err(stderr)
                         .option("engine.WarnInterpreterOnly", "false")
                         .build()) {


                context.eval("js", script);

                result.setOutput(outContent.toString());
                result.setError(errContent.toString());
                result.setStatus(COMPLETED);

            } catch (PolyglotException e) {
                if(e.isInterrupted()) {
                    result.setError(e.getMessage());
                    result.setStatus(STOPPED);
                }else {
                    result.setError(e.getMessage());
                    result.setStatus(ERROR);
                }
            }
            result.setId(scriptInfo.getId());
            scriptInfo.setResult(MapperScript.mapToScriptResultDTO(result));
            scriptInfo.setDuration(System.currentTimeMillis() - scriptInfo.getStartTime());

            return result;
        };
    }

    public List<ScriptInfoShort> getAllScripts(SortedByTime sortTime, SortedByStatus sortStatus) {

        return scriptStorage.values().stream()
                .map(MapperScript::mapToShortInfo)
                .peek(scriptInfoShort ->  {
                    ByteArrayOutputStream byteArrayOutputStream = outputStorage.get(scriptInfoShort.getId());
                    scriptInfoShort.getResult().setOutput(byteArrayOutputStream.toString());
                })
                .filter(scriptInfoShort -> filterByStatus(scriptInfoShort, sortStatus))
                .sorted(getComparator(sortTime))
                .toList();
    }

    private boolean filterByStatus(ScriptInfoShort scriptInfoShort, SortedByStatus sortStatus) {
        if (sortStatus == SortedByStatus.ALL) {
            return true;
        }
        return scriptInfoShort.getResult() != null && scriptInfoShort.getResult().getStatus().name().equals(sortStatus.name());
    }

    private Comparator<ScriptInfoShort> getComparator(SortedByTime sortTime) {
        return switch (sortTime) {
            case UPPER -> Comparator.comparing(ScriptInfoShort::getDuration);
            case LOWER -> Comparator.comparing(ScriptInfoShort::getDuration).reversed();
            default -> Comparator.comparing(ScriptInfoShort::getId);
        };
    }

    public ScriptInfoResponse getInfoScriptById(String id) {
        ScriptInfo scriptInfo = scriptStorage.get(id);
        if(scriptInfo == null) {
            throw new NotFoundException(ScriptResponse.builder()
                    .message(MESSAGE_SCRIPT_NOT_FOUND)
                    .id(id)
                    .statusOperation(StatusOperation.ERROR)
                    .build());
        }

        ByteArrayOutputStream byteArrayOutputStream = outputStorage.get(scriptInfo.getId());
        scriptInfo.getResult().setOutput(byteArrayOutputStream.toString());

        return MapperScript.mapToResponseScript(scriptInfo);
    }

    public void stopScript(String scriptId) {
        ScriptInfo scriptInfo = scriptStorage.get(scriptId);
        if (scriptInfo == null) {
            throw new NotFoundException(ScriptResponse.builder()
                    .message(MESSAGE_SCRIPT_NOT_FOUND)
                    .id(scriptId)
                    .statusOperation(StatusOperation.ERROR)
                    .build());
        }

        Future<?> future = scriptInfo.getFuture();
        if(scriptInfo.getResult().getStatus() == PROCESSING) {
            if (future != null) {
                future.cancel(true);
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, ScriptResponse.builder()
                        .message(MESSAGE_SCRIPT_NOT_RUNNING)
                        .id(scriptId)
                        .statusOperation(StatusOperation.ERROR)
                        .build());
            }
        }else {
            throw new ApplicationException(HttpStatus.CONFLICT,
                    ScriptResponse.builder().message(MESSAGE_SCRIPT_NOT_RUNNING).id(scriptId).statusOperation(StatusOperation.ERROR).build());
        }
    }

    public void deleteScript(String id) {
        ScriptInfo scriptInfo = scriptStorage.get(id);
        if (scriptInfo == null) {
            throw new NotFoundException(ScriptResponse.builder()
                    .message(MESSAGE_SCRIPT_NOT_FOUND)
                    .id(id)
                    .statusOperation(StatusOperation.ERROR)
                    .build());
        }
        if(scriptInfo.getResult() != null) {
            Status status = scriptInfo.getResult().getStatus();

            if (status != PROCESSING) {
                scriptStorage.remove(id);
                outputStorage.remove(id);
            } else {
                throw new ScriptRunException(ScriptResponse.builder()
                        .message(MESSAGE_SCRIPT_RUNNING)
                        .id(id)
                        .statusOperation(StatusOperation.ERROR)
                        .build());
            }
        }
    }

}
