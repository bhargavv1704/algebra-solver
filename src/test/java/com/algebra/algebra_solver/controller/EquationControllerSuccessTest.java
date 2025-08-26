package com.algebra.algebra_solver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquationControllerSuccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void storeAndRetrieveValidEquation() throws Exception {
        String eqJson = "{\"equation\": \"3*x + 2\"}";
        String resJson = mockMvc.perform(post("/api/equations/store")
                .contentType(MediaType.APPLICATION_JSON)
                .content(eqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Equation stored successfully"))
                .andReturn().getResponse().getContentAsString();

        String equationId = resJson.replaceAll(".*\"equationId\":\"(\\d+)\".*", "$1");

        mockMvc.perform(get("/api/equations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equations[?(@.equationId=='" + equationId + "')].equation")
                        .value("3 * x + 2"));

    }

    @Test
    void evaluateValidEquation_returnsCorrectResult() throws Exception {
        String storeJson = "{\"equation\": \"3*x + 2*y - z\"}";
        String storeRes = mockMvc.perform(post("/api/equations/store")
                .contentType(MediaType.APPLICATION_JSON)
                .content(storeJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String equationId = storeRes.replaceAll(".*\"equationId\":\"(\\d+)\".*", "$1");

        String evalJson = "{\"variables\": {\"x\": 2, \"y\": 3, \"z\": 1}}";
        mockMvc.perform(post("/api/equations/" + equationId + "/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(evalJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(11.0));
    }
}
