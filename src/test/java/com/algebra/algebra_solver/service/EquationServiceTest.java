package com.algebra.algebra_solver.service;

import com.algebra.algebra_solver.exception.InvalidEquationException;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EquationServiceTest {

    private final EquationService svc = new EquationServiceImpl();

    @Test
    void evaluate_with_variables() {
        String id = svc.store("3x + 2y - z").getId();
        double v = svc.evaluate(id, Map.of("x", 2.0, "y", 3.0, "z", 1.0));
        assertEquals(11.0, v, 1e-9);
    }

    @Test
    void solve_linear() {
        String id = svc.store("3x + 2").getId();
        EquationService.SolveResult s = svc.solve(id, "x", Map.of());
        assertEquals(1, s.degree);
        assertEquals(-2.0 / 3.0, s.solutions[0], 1e-9);
    }

    @Test
    void solve_quadratic() {
        String id = svc.store("x^2 - 5x + 6").getId();
        EquationService.SolveResult s = svc.solve(id, "x", Map.of());
        assertEquals(2, s.degree);
        double a = s.solutions[0], b = s.solutions[1];
        assertTrue((Math.abs(a - 2) < 1e-9 && Math.abs(b - 3) < 1e-9) ||
                (Math.abs(a - 3) < 1e-9 && Math.abs(b - 2) < 1e-9));
    }

    // --- EXTRA EDGE CASE TESTS ---
    @Test
    void store_and_retrieve() {
        var eq = svc.store("3*x + 2");
        assertNotNull(eq.getId());
        assertEquals(eq, svc.get(eq.getId()));
    }

    @Test
    void invalid_equation_should_fail() {
        assertThrows(InvalidEquationException.class, () -> svc.store("3**x"));
    }

    @Test
    void missing_variable_should_fail() {
        var eq = svc.store("x + y");
        Map<String, Double> vars = new HashMap<>();
        vars.put("x", 2.0);
        assertThrows(IllegalArgumentException.class, () -> svc.evaluate(eq.getId(), vars));
    }

    @Test
    void division_by_zero_should_fail() {
        var eq = svc.store("1/x");
        Map<String, Double> vars = new HashMap<>();
        vars.put("x", 0.0);
        assertThrows(ArithmeticException.class, () -> svc.evaluate(eq.getId(), vars));
    }

    // ---------- ADDED FOR EXTRA RIGOR ----------

    @Test
    void evaluate_with_parentheses_and_nesting() {
        // (2 + x) * (3 + y)
        String id = svc.store("(2 + x) * (3 + y)").getId();
        double v = svc.evaluate(id, Map.of("x", 1.0, "y", 2.0));
        assertEquals(15.0, v, 1e-9);
    }

    @Test
    void solve_with_multiple_unknowns_should_fail() {
        String id = svc.store("2*x + 3*y").getId();
        assertThrows(InvalidEquationException.class, () -> svc.solve(id, "x", Map.of()));
    }

    @Test
    void store_and_evaluate_very_long_expression() {
        String expr = "x^3 + 2*x^2 - x + (y - 1)*(z + 4) - 7/5 + (x^2)";
        String id = svc.store(expr).getId();
        assertNotNull(id);
        // This just checks that evaluation doesn't throw for a complex input.
        assertDoesNotThrow(() -> svc.evaluate(id, Map.of("x", 1.0, "y", 2.0, "z", 3.0)));
    }

    @Test
    void evaluate_with_no_variables_provided_should_fail() {
        String id = svc.store("x + y").getId();
        assertThrows(IllegalArgumentException.class, () -> svc.evaluate(id, new HashMap<>()));
    }
}
