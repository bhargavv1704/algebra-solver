package com.algebra.algebra_solver.model;

import java.util.Map;
import java.util.Set;

public class OperandNode extends Node {
    private final String token; // either number like "3.5" or variable name like "x"

    public OperandNode(String token) {
        this.token = token;
    }

    public boolean isNumber() {
        return token.matches("-?\\d+(\\.\\d+)?");
    }

    public String getToken() {
        return token;
    }

    @Override
    public double evaluate(Map<String, Double> vars) {
        if (isNumber())
            return Double.parseDouble(token);
        Double v = vars.get(token);
        if (v == null)
            throw new IllegalArgumentException("Missing variable value for '" + token + "'");
        return v;
    }

    @Override
    public String toInfix() {
        if (isNumber()) {
            double d = Double.parseDouble(token);
            if (d == Math.rint(d))
                return String.valueOf((long) d); // print integer without .0
            return token;
        } else {
            return token;
        }
    }

    @Override
    public void collectVariables(Set<String> acc) {
        if (!isNumber())
            acc.add(token);
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }
}
