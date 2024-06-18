package com.offer.java.offer.service;

import com.offer.java.offer.dto.*;
import com.offer.java.offer.exception.ApplicationException;
import com.offer.java.offer.exception.NotFoundException;
import com.offer.java.offer.exception.ScriptRunException;
import com.offer.java.offer.mapper.MapperScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static com.offer.java.offer.dto.Status.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScriptExecutionServiceTest {
    @InjectMocks
    private ScriptExecutionService scriptExecutionService;

    private ConcurrentHashMap<String, ScriptInfo> scriptStorage;

    private static final String MESSAGE_SCRIPT_NOT_FOUND = "Script with this id - %s not found";
    private static final String MESSAGE_SCRIPT_RUNNING = "The script is currently running and cannot be deleted";
    private static final String MESSAGE_SCRIPT_NOT_RUNNING = "Script with is not running";

    @BeforeEach
    void setUp() {
        scriptStorage = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(scriptExecutionService, "scriptStorage", scriptStorage);
    }


    @Test
    public void testExecuteScript_Blocking() {

        String script = "console.log('lol')";
        boolean isBlocking = true;


        ScriptResult result = scriptExecutionService.executeScript(script, isBlocking);

        assertNotNull(result);
        assertEquals(COMPLETED, result.getStatus());
    }

    @Test
    public void testExecuteScript_Error(){

        String script = "consosdle.log('lol')";
        boolean isBlocking = true;

        ScriptResult result = scriptExecutionService.executeScript(script, isBlocking);

        assertNotNull(result);
        assertEquals(ERROR, result.getStatus());
    }


    @Test
    public void testExecuteScript_isBlockingFalse_noCorrectScript() {

        String script = "consosdle.log('lol')";
        boolean isBlocking = false;

        ScriptResult result = scriptExecutionService.executeScript(script, isBlocking);

        assertNotNull(result);
        assertEquals(QUEUE, result.getStatus());
    }

    @Test
    public void testExecuteScript_isBlockingFalse_correctScript() {

        String script = "console.log('lol')";
        boolean isBlocking = false;

        ScriptResult result = scriptExecutionService.executeScript(script, isBlocking);

        assertNotNull(result);
        assertEquals(QUEUE, result.getStatus());
    }

    @Test
    public void testGetAllScripts_EmptyStorage() {

        List<ScriptInfoShort> result = scriptExecutionService.getAllScripts(SortedByTime.DEFAULT, SortedByStatus.ALL);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetAllScripts_FilterAndSort() {
        ScriptInfo script1 = new ScriptInfo("1", "script1");
        ScriptInfo script2 = new ScriptInfo("2", "script2");
        ScriptInfo script3 = new ScriptInfo("3", "script3");

        ScriptInfoShort shortInfo1 = new ScriptInfoShort();
        shortInfo1.setScript("script1");
        shortInfo1.setId("1");
        shortInfo1.setResult(new ScriptResultDTO());
        ScriptInfoShort shortInfo2 = new ScriptInfoShort();
        shortInfo2.setScript("script2");
        shortInfo2.setId("2");
        shortInfo2.setResult(new ScriptResultDTO());
        ScriptInfoShort shortInfo3 = new ScriptInfoShort();
        shortInfo3.setScript("script3");
        shortInfo3.setId("3");
        shortInfo3.setResult(new ScriptResultDTO());

        scriptStorage.put("1", script1);
        scriptStorage.put("2", script2);
        scriptStorage.put("3", script3);

        List<ScriptInfoShort> result = scriptExecutionService.getAllScripts(SortedByTime.UPPER, SortedByStatus.ALL);

        assertEquals(Arrays.asList(shortInfo1, shortInfo2, shortInfo3), result);
    }

    @Test
    void testGetInfoScriptById_Success() {
        try (var mockedMapperScript = mockStatic(MapperScript.class)) {

            String scriptId = "1";
            ScriptInfo scriptInfo = new ScriptInfo(scriptId, "script1");
            ScriptInfoResponse expectedResponse = new ScriptInfoResponse();

            scriptStorage.put(scriptId, scriptInfo);

            mockedMapperScript.when(() -> MapperScript.mapToResponseScript(scriptInfo)).thenReturn(expectedResponse);

            ScriptInfoResponse result = scriptExecutionService.getInfoScriptById(scriptId);

            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void testGetInfoScriptById_NotFound() {
        String scriptId = "1";

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            scriptExecutionService.getInfoScriptById(scriptId);
        });

        assertEquals(String.format(MESSAGE_SCRIPT_NOT_FOUND, scriptId), thrown.getMessage());
    }


    @Test
    void testStopScript_ScriptNotFound() {
        String scriptId = "1";

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            scriptExecutionService.stopScript(scriptId);
        });

        assertEquals(String.format(MESSAGE_SCRIPT_NOT_FOUND, scriptId), thrown.getMessage());
    }

    @Test
    void testStopScript_ScriptNotProcessing() {
        String scriptId = "1";
        String script = "script";
        ScriptInfo scriptInfo = new ScriptInfo(scriptId,script);
        scriptInfo.setFuture(mock(Future.class));
        scriptStorage.put(scriptId, scriptInfo);

        ApplicationException thrown = assertThrows(ApplicationException.class, () -> {
            scriptExecutionService.stopScript(scriptId);
        });

        assertEquals(MESSAGE_SCRIPT_NOT_RUNNING, thrown.getMessage());
        assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
    }

    @Test
    void testStopScript_ScriptProcessingAndFutureNotNull() {
        String scriptId = "1";
        String script = "script";
        Future<?> futureMock = mock(Future.class);

        ScriptResultDTO scriptResult = new ScriptResultDTO();
        scriptResult.setStatus(PROCESSING);

        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptInfo.setFuture(futureMock);
        scriptInfo.setResult(scriptResult);
        scriptStorage.put(scriptId, scriptInfo);

        scriptExecutionService.stopScript(scriptId);

        verify(futureMock).cancel(true);
    }

    @Test
    void testStopScript_ScriptProcessingAndFutureIsNull() {
        String scriptId = "1";
        String script = "script";

        ScriptResultDTO scriptResult = new ScriptResultDTO();
        scriptResult.setStatus(PROCESSING);

        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptInfo.setResult(scriptResult);

        scriptStorage.put(scriptId, scriptInfo);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            scriptExecutionService.stopScript(scriptId);
        });

        assertEquals(MESSAGE_SCRIPT_NOT_RUNNING, thrown.getMessage());
    }

    @Test
    void testDeleteScript_ScriptNotFound() {
        String scriptId = "1";

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            scriptExecutionService.deleteScript(scriptId);
        });

        assertEquals(String.format(MESSAGE_SCRIPT_NOT_FOUND, scriptId), thrown.getMessage());
    }

    @Test
    void testDeleteScript_ScriptResultIsNull() {
        String scriptId = "1";
        String script = "script";
        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptStorage.put(scriptId, scriptInfo);

        scriptExecutionService.deleteScript(scriptId);

        assertNull(scriptStorage.get(scriptId));
    }

    @Test
    void testDeleteScript_ScriptNotProcessing() {
        String scriptId = "1";
        String script = "script";

        ScriptResultDTO scriptResult = new ScriptResultDTO();
        scriptResult.setStatus(COMPLETED);

        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptInfo.setResult(scriptResult);
        scriptStorage.put(scriptId, scriptInfo);

        scriptExecutionService.deleteScript(scriptId);

        assertNull(scriptStorage.get(scriptId));
    }

    @Test
    void testDeleteScript_ScriptProcessing() {
        String scriptId = "1";
        String script = "script";

        ScriptResultDTO scriptResult = new ScriptResultDTO();
        scriptResult.setStatus(PROCESSING);

        ScriptInfo scriptInfo = new ScriptInfo(scriptId, script);
        scriptInfo.setResult(scriptResult);

        scriptStorage.put(scriptId, scriptInfo);

        ScriptRunException thrown = assertThrows(ScriptRunException.class, () -> {
            scriptExecutionService.deleteScript(scriptId);
        });

        assertEquals(MESSAGE_SCRIPT_RUNNING, thrown.getMessage());
    }


}
