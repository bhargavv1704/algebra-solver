package com.algebra.algebra_solver.controller;

import com.algebra.algebra_solver.model.Equation;
import com.algebra.algebra_solver.service.EquationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/equations")
public class EquationController {

    private final EquationService service;

    public EquationController(EquationService service) {
        this.service = service;
    }

    public static class StoreRequest {
        public String equation;
    }

    public static class EvaluateRequest {
        public Map<String, Double> variables;
    }

    public static class SolveRequest {
        public String variable;
        public Map<String, Double> knowns;
    }

    @PostMapping(path = "/store", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> store(@RequestBody StoreRequest req) {
        Equation e = service.store(req.equation);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Equation stored successfully");
        body.put("equationId", e.getId());
        return body;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> list() {
        List<Map<String, Object>> arr = new ArrayList<>();
        for (Equation e : service.list()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("equationId", e.getId());
            row.put("equation", e.getInfix());
            arr.add(row);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("equations", arr);
        return body;
    }

    // 🔹 Get by ID
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        Equation e = service.get(id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("equationId", e.getId());
        body.put("equation", e.getInfix());
        body.put("variables", e.getVariables());
        return ResponseEntity.ok(body);
    }

    @PostMapping(path = "/{id}/evaluate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> evaluate(@PathVariable String id, @RequestBody EvaluateRequest req) {
        Equation e = service.get(id);
        double result = service.evaluate(id, req == null ? Collections.emptyMap() : req.variables);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("equationId", e.getId());
        body.put("equation", e.getInfix());
        body.put("variables", req == null ? Collections.emptyMap() : req.variables);
        body.put("result", result);
        return body;
    }

    @PostMapping(path = "/{id}/solve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> solve(@PathVariable String id, @RequestBody(required = false) SolveRequest req) {
        String variable = (req == null || req.variable == null || req.variable.isBlank()) ? "x" : req.variable;
        Map<String, Double> knowns = (req == null || req.knowns == null) ? Collections.emptyMap() : req.knowns;
        EquationService.SolveResult s = service.solve(id, variable, knowns);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("equationId", s.equationId);
        body.put("equation", s.equation);
        body.put("variable", s.variable);
        body.put("degree", s.degree);
        Map<String, Object> coeffs = new LinkedHashMap<>();
        coeffs.put("a", s.a);
        coeffs.put("b", s.b);
        coeffs.put("c", s.c);
        body.put("coefficients", coeffs);
        body.put("solutions", s.solutions);
        return body;
    }

    // 🔹 Delete
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> delete(@PathVariable String id) {
        service.delete(id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Equation deleted successfully");
        body.put("equationId", id);
        return body;
    }
}
