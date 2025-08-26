package com.algebra.algebra_solver.model;

import java.util.Map;
import java.util.Set;

public class OperatorNode extends Node {
    private final String op;
    private final Node left, right;

    public OperatorNode(String op, Node left, Node right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public double evaluate(Map<String, Double> vars) {
        double a = left.evaluate(vars);
        double b = right.evaluate(vars);
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (Math.abs(b) < 1e-12)
                    throw new ArithmeticException("Division by zero");
                yield a / b;
            }
            case "^" -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }

    private static int precedenceOf(String o) {
        return switch (o) {
            case "^" -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            default -> -1;
        };
    }

    private static boolean rightAssociative(String o) {
        return "^".equals(o);
    }

    @Override
    public String toInfix() {
        String L = left.toInfix();
        String R = right.toInfix();

        if (left instanceof OperatorNode) {
            OperatorNode lo = (OperatorNode) left;
            if (precedenceOf(lo.op) < precedenceOf(this.op))
                L = "(" + L + ")";
        }

        if (right instanceof OperatorNode) {
            OperatorNode ro = (OperatorNode) right;
            if (precedenceOf(ro.op) < precedenceOf(this.op) ||
                    (precedenceOf(ro.op) == precedenceOf(this.op) && !rightAssociative(this.op))) {
                R = "(" + R + ")";
            }
        }

        return L + " " + op + " " + R;
    }

    @Override
    public void collectVariables(Set<String> acc) {
        left.collectVariables(acc);
        right.collectVariables(acc);
    }

    @Override
    public int precedence() {
        return precedenceOf(op);
    }
}
