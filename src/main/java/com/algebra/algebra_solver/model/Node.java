package com.algebra.algebra_solver.model;

import java.util.Map;
import java.util.Set;

public abstract class Node {
    /** Evaluate the subtree given variable assignments. */
    public abstract double evaluate(Map<String, Double> vars);

    /** Produce a readable infix string (with minimal parentheses). */
    public abstract String toInfix();

    /** Collect variable names used in this subtree. */
    public abstract void collectVariables(Set<String> acc);

    /** Precedence used to decide where parentheses are needed. */
    public int precedence() {
        return Integer.MAX_VALUE;
    }
}
