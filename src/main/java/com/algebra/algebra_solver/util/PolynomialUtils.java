package com.algebra.algebra_solver.util;

import com.algebra.algebra_solver.exception.InvalidEquationException;
import com.algebra.algebra_solver.model.Node;
import com.algebra.algebra_solver.model.OperandNode;
import com.algebra.algebra_solver.model.OperatorNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Convert expression tree into polynomial coefficients for a specified
 * variable,
 * substituting other variables using 'knowns'. Supports degree up to 2 for
 * solving.
 */
public final class PolynomialUtils {
    private PolynomialUtils() {
    }

    public static Map<Integer, Double> toPolynomial(Node node, String var, Map<String, Double> knowns) {
        Map<Integer, Double> poly = build(node, var, knowns);
        poly.replaceAll((k, v) -> Math.abs(v) < 1e-12 ? 0.0 : v);
        return poly;
    }

    private static Map<Integer, Double> build(Node n, String var, Map<String, Double> knowns) {
        if (n instanceof OperandNode o) {
            String t = o.getToken();
            if (t.matches("-?\\d+(\\.\\d+)?")) {
                return constPoly(Double.parseDouble(t));
            } else {
                if (t.equals(var)) {
                    Map<Integer, Double> m = new HashMap<>();
                    m.put(1, 1.0);
                    return m;
                } else {
                    if (knowns == null || !knowns.containsKey(t)) {
                        throw new InvalidEquationException(
                                "Missing known value for '" + t + "' while solving for '" + var + "'");
                    }
                    return constPoly(knowns.get(t));
                }
            }
        }

        if (n instanceof OperatorNode op) {
            String o = op.getOp();
            Map<Integer, Double> L = build(op.getLeft(), var, knowns);
            Map<Integer, Double> R = build(op.getRight(), var, knowns);
            return switch (o) {
                case "+" -> add(L, R);
                case "-" -> add(L, scale(R, -1));
                case "*" -> multiply(L, R);
                case "/" -> {
                    if (R.size() == 1 && R.containsKey(0)) {
                        double denom = R.get(0);
                        if (Math.abs(denom) < 1e-12)
                            throw new InvalidEquationException("Division by zero");
                        yield scale(L, 1.0 / denom);
                    }
                    throw new InvalidEquationException("Division by non-constant not supported for solving");
                }
                case "^" -> {
                    int exp = smallIntConst(R);
                    if (exp < 0 || exp > 3)
                        throw new InvalidEquationException(
                                "Exponent must be small integer 0..3 for polynomial construction");
                    yield powPoly(L, exp);
                }
                default -> throw new InvalidEquationException("Unsupported operator in polynomial build: " + o);
            };
        }

        throw new InvalidEquationException("Unsupported node type in polynomial build");
    }

    private static Map<Integer, Double> constPoly(double c) {
        Map<Integer, Double> m = new HashMap<>();
        m.put(0, c);
        return m;
    }

    private static Map<Integer, Double> add(Map<Integer, Double> A, Map<Integer, Double> B) {
        Map<Integer, Double> out = new HashMap<>(A);
        for (Map.Entry<Integer, Double> e : B.entrySet())
            out.merge(e.getKey(), e.getValue(), Double::sum);
        return out;
    }

    private static Map<Integer, Double> scale(Map<Integer, Double> A, double s) {
        Map<Integer, Double> out = new HashMap<>();
        for (var e : A.entrySet())
            out.put(e.getKey(), e.getValue() * s);
        return out;
    }

    private static Map<Integer, Double> multiply(Map<Integer, Double> A, Map<Integer, Double> B) {
        Map<Integer, Double> out = new HashMap<>();
        for (var ea : A.entrySet())
            for (var eb : B.entrySet()) {
                int deg = ea.getKey() + eb.getKey();
                out.merge(deg, ea.getValue() * eb.getValue(), Double::sum);
            }
        return out;
    }

    private static int smallIntConst(Map<Integer, Double> poly) {
        if (poly.size() == 1 && poly.containsKey(0)) {
            double v = poly.get(0);
            int iv = (int) Math.round(v);
            if (Math.abs(v - iv) < 1e-9)
                return iv;
        }
        return -1;
    }

    private static Map<Integer, Double> powPoly(Map<Integer, Double> base, int exp) {
        Map<Integer, Double> out = constPoly(1.0);
        for (int i = 0; i < exp; i++)
            out = multiply(out, base);
        return out;
    }
}
