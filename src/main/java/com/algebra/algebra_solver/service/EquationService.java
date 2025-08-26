package com.algebra.algebra_solver.service;

import com.algebra.algebra_solver.model.Equation;

import java.util.List;
import java.util.Map;

public interface EquationService {
    Equation store(String infix);

    Equation get(String id);

    List<Equation> list();

    double evaluate(String id, Map<String, Double> vars);

    SolveResult solve(String id, String variable, Map<String, Double> knowns);

    //  new
    void delete(String id);

    class SolveResult {
        public String equationId;
        public String equation;
        public String variable;
        public int degree;
        public double a, b, c;
        public double[] solutions;
    }
}
