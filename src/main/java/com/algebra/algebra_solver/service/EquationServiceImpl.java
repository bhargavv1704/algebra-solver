package com.algebra.algebra_solver.service;

import com.algebra.algebra_solver.exception.EquationNotFoundException;
import com.algebra.algebra_solver.exception.InvalidEquationException;
import com.algebra.algebra_solver.model.Equation;
import com.algebra.algebra_solver.model.Node;
import com.algebra.algebra_solver.util.EquationParser;
import com.algebra.algebra_solver.util.PolynomialUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EquationServiceImpl implements EquationService {
    private final Map<String, Equation> store = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    @Override
    public Equation store(String infix) {
        Node root = EquationParser.parseToTree(infix);
        Set<String> vars = new TreeSet<>();
        root.collectVariables(vars);
        String id = String.valueOf(seq.incrementAndGet());
        Equation e = new Equation(id, root.toInfix(), root, vars);
        store.put(id, e);
        return e;
    }

    @Override
    public Equation get(String id) {
        Equation e = store.get(id);
        if (e == null)
            throw new EquationNotFoundException("Equation id " + id + " not found");
        return e;
    }

    @Override
    public List<Equation> list() {
        return new ArrayList<>(store.values());
    }

    @Override
    public double evaluate(String id, Map<String, Double> vars) {
        Equation e = get(id);
        return e.getRoot().evaluate(vars == null ? Collections.emptyMap() : vars);
    }

    @Override
    public SolveResult solve(String id, String variable, Map<String, Double> knowns) {
        if (variable == null || variable.isBlank())
            variable = "x";
        Equation e = get(id);
        Map<Integer, Double> poly = PolynomialUtils.toPolynomial(e.getRoot(), variable,
                knowns == null ? Collections.emptyMap() : knowns);

        int deg = poly.keySet().stream().max(Integer::compareTo).orElse(0);
        if (deg > 2)
            throw new InvalidEquationException("Only linear/quadratic equations supported");

        double a = poly.getOrDefault(2, 0.0);
        double b = poly.getOrDefault(1, 0.0);
        double c = poly.getOrDefault(0, 0.0);

        SolveResult res = new SolveResult();
        res.equationId = e.getId();
        res.equation = e.getInfix();
        res.variable = variable;
        res.degree = deg;
        res.a = a;
        res.b = b;
        res.c = c;

        if (deg == 0) {
            if (Math.abs(c) < 1e-12) {
                res.solutions = new double[] {};
                return res;
            }
            throw new InvalidEquationException("No solution (constant != 0)");
        }
        if (deg == 1) {
            if (Math.abs(b) < 1e-12)
                throw new InvalidEquationException("Invalid linear equation (b=0)");
            res.solutions = new double[] { -c / b };
            return res;
        }
        // quadratic
        double disc = b * b - 4 * a * c;
        if (disc < -1e-12)
            throw new InvalidEquationException("No real roots (discriminant < 0)");
        double sqrt = Math.sqrt(Math.max(0.0, disc));
        res.solutions = new double[] { (-b - sqrt) / (2 * a), (-b + sqrt) / (2 * a) };
        return res;
    }

    // 🔹 new
    @Override
    public void delete(String id) {
        if (!store.containsKey(id))
            throw new EquationNotFoundException("Equation id " + id + " not found");
        store.remove(id);
    }
}
