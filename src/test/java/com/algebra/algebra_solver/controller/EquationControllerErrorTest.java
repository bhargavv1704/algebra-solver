package com.algebra.algebra_solver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquationControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void notFound_returns404() throws Exception {
        // ID 9999 doesn’t exist
        mockMvc.perform(post("/api/equations/9999/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variables\":{\"x\":2}}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void invalidEquation_returns400() throws Exception {
        // Try to store an invalid equation
        mockMvc.perform(post("/api/equations/store")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"equation\":\"3x ++ 2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Invalid")));
    }

    @Test
    void unsolvableEquation_returns422() throws Exception {
        // Equation with no real solution: x^2 + 1 = 0
        String storeJson = mapper.writeValueAsString(Map.of("equation", "x^2 + 1"));
        String id = mockMvc.perform(post("/api/equations/store")
                .contentType(MediaType.APPLICATION_JSON)
                .content(storeJson))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"equationId\":\"(\\d+)\".*", "$1");

        mockMvc.perform(post("/api/equations/" + id + "/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variable\":\"x\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message", containsString("No real roots")));
    }
}
