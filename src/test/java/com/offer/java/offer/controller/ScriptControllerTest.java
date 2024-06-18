package com.offer.java.offer.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offer.java.offer.dto.*;
import com.offer.java.offer.service.ScriptExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScriptController.class)
public class ScriptControllerTest {
    @MockBean
    private ScriptExecutionService service;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testExecuteScript_blockingTrue_returnsScriptResult() throws Exception {

        String script = "console.log('fdsfd')";
        boolean blocking = true;
        ScriptResult scriptResult = new ScriptResult();
        scriptResult.setStatus(Status.COMPLETED);

        when(service.executeScript(script, blocking)).thenReturn(scriptResult);

        mockMvc.perform(post("/api/scripts/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("blocking", String.valueOf(blocking))
                        .content(script))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(scriptResult.getStatus().toString())));
    }

    @Test
    public void testExecuteScript_blockingFalse_returnsScriptResult() throws Exception {

        String script = "console.log('fdsfd')";
        boolean blocking = false;
        ScriptResult scriptResult = new ScriptResult();
        scriptResult.setStatus(Status.COMPLETED);

        when(service.executeScript(script, blocking)).thenReturn(scriptResult);

        mockMvc.perform(post("/api/scripts/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("blocking", String.valueOf(blocking))
                        .content(script))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(scriptResult.getStatus().toString())));
    }


    @Test
    public void testListScripts_defaultSorting_returnsScriptInfoList() throws Exception {

        List<ScriptInfoShort> expectedScripts = Arrays.asList(new ScriptInfoShort(), new ScriptInfoShort());
        when(service.getAllScripts(SortedByTime.DEFAULT, SortedByStatus.ALL))
                .thenReturn(expectedScripts);

        MvcResult result = mockMvc.perform(get("/api/scripts"))
                .andExpect(status().isOk())
                .andReturn();

        List<ScriptInfoShort> actualScripts = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(expectedScripts, actualScripts);
    }

    @Test
    public void testListScripts_sortedByTime_returnsScriptInfoList() throws Exception {
        ScriptInfoShort scriptInfoShort1 = new ScriptInfoShort();
        scriptInfoShort1.setDuration(200);

        ScriptInfoShort scriptInfoShort2 = new ScriptInfoShort();
        scriptInfoShort2.setDuration(100);

        List<ScriptInfoShort> expectedScripts = Arrays.asList(scriptInfoShort1, scriptInfoShort2);
        when(service.getAllScripts(SortedByTime.UPPER, SortedByStatus.ALL))
                .thenReturn(expectedScripts);

        MvcResult result = mockMvc.perform(get("/api/scripts")
                        .param("sortedTime", "UPPER"))
                .andExpect(status().isOk())
                .andReturn();

        List<ScriptInfoShort> actualScripts = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(expectedScripts, actualScripts);
    }

    @Test
    public void testListScripts_sortedByStatus_returnsScriptInfoList() throws Exception {

        ScriptInfoShort scriptInfoShort1 = new ScriptInfoShort();
        ScriptResultDTO scriptResultDTO1 = new ScriptResultDTO();
        scriptResultDTO1.setStatus(Status.QUEUE);
        scriptInfoShort1.setResult(scriptResultDTO1);

        ScriptInfoShort scriptInfoShort2 = new ScriptInfoShort();
        ScriptResultDTO scriptResultDTO2 = new ScriptResultDTO();
        scriptResultDTO2.setStatus(Status.QUEUE);
        scriptInfoShort2.setResult(scriptResultDTO2);

        List<ScriptInfoShort> expectedScripts = Arrays.asList(scriptInfoShort1, scriptInfoShort2);
        when(service.getAllScripts(SortedByTime.DEFAULT, SortedByStatus.QUEUE))
                .thenReturn(expectedScripts);

        MvcResult result = mockMvc.perform(get("/api/scripts")
                        .param("sortedStatus", Status.QUEUE.toString()))
                .andExpect(status().isOk())
                .andReturn();

        List<ScriptInfoShort> actualScripts = mapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertEquals(expectedScripts, actualScripts);
    }


    @Test
    public void testGetScriptInfo_returnScriptInfoResponse() throws Exception {

        String id = "id-test";
        ScriptInfoResponse scriptInfoResponse = new ScriptInfoResponse();
        scriptInfoResponse.setId("id-test");

        when(service.getInfoScriptById(id)).thenReturn(scriptInfoResponse);

        mockMvc.perform(get("/api/scripts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("id-test")));
    }

    @Test
    public void testStopScript_returnResponseScriptResponse() throws Exception {
        String id = "script-id";

        mockMvc.perform(delete("/api/scripts/{id}/stop", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{'answer': 'Script stopped'}"));
    }

    @Test
    public void testRemoveScript_returnResponseScriptResponse() throws Exception  {
        String id = "script-id";

        mockMvc.perform(delete("/api/scripts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{'answer': 'Script deleted'}"));
    }

}
