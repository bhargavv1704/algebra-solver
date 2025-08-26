package com.algebra.algebra_solver.util;

import com.algebra.algebra_solver.exception.InvalidEquationException;
import com.algebra.algebra_solver.model.Node;
import com.algebra.algebra_solver.model.OperandNode;
import com.algebra.algebra_solver.model.OperatorNode;

import java.util.*;

public final class EquationParser {

    private EquationParser() {
    }

    // Public: parse infix into expression tree (Node)
    public static Node parseToTree(String raw) {
        if (raw == null || raw.trim().isEmpty())
            throw new InvalidEquationException("Equation cannot be empty");
        List<Token> tokens = tokenize(raw);
        tokens = insertImplicitMultiplication(tokens);
        List<Token> post = infixToPostfix(tokens);
        return buildTree(post);
    }

    // Token representation
    private enum Type {
        NUMBER, IDENT, OP, LPAREN, RPAREN
    }

    private static class Token {
        final Type type;
        final String text;

        Token(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        public String toString() {
            return type + ":" + text;
        }
    }

    private static boolean isIdentStart(char c) {
        return Character.isLetter(c);
    }

    private static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private static List<Token> tokenize(String s) {
        List<Token> out = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (isDigit(c) || (c == '.' && i + 1 < n && isDigit(s.charAt(i + 1)))) {
                int j = i + 1;
                boolean dot = (c == '.');
                while (j < n) {
                    char d = s.charAt(j);
                    if (isDigit(d)) {
                        j++;
                        continue;
                    }
                    if (d == '.' && !dot) {
                        dot = true;
                        j++;
                        continue;
                    }
                    break;
                }
                out.add(new Token(Type.NUMBER, s.substring(i, j)));
                i = j;
                continue;
            }
            // unary minus before number: capture -3
            if (c == '-' && (out.isEmpty() || out.get(out.size() - 1).type == Type.OP
                    || out.get(out.size() - 1).type == Type.LPAREN)) {
                int j = i + 1;
                if (j < n && (isDigit(s.charAt(j)) || (s.charAt(j) == '.' && j + 1 < n && isDigit(s.charAt(j + 1))))) {
                    int k = j + 1;
                    boolean dot = (s.charAt(j) == '.');
                    while (k < n) {
                        char d = s.charAt(k);
                        if (isDigit(d)) {
                            k++;
                            continue;
                        }
                        if (d == '.' && !dot) {
                            dot = true;
                            k++;
                            continue;
                        }
                        break;
                    }
                    out.add(new Token(Type.NUMBER, s.substring(i, k)));
                    i = k;
                    continue;
                }
            }
            if (isIdentStart(c)) {
                int j = i + 1;
                while (j < n && (Character.isLetterOrDigit(s.charAt(j))))
                    j++;
                out.add(new Token(Type.IDENT, s.substring(i, j)));
                i = j;
                continue;
            }
            switch (c) {
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                    out.add(new Token(Type.OP, String.valueOf(c)));
                    i++;
                    break;
                case '(':
                    out.add(new Token(Type.LPAREN, "("));
                    i++;
                    break;
                case ')':
                    out.add(new Token(Type.RPAREN, ")"));
                    i++;
                    break;
                default:
                    throw new InvalidEquationException("Invalid character: " + c);
            }
        }
        return out;
    }

    // implicit multiplication 3x -> 3 * x, 2(x+1) -> 2*(x+1), x(y+1) -> x*(y+1), )(
    // -> )*( etc.
    private static List<Token> insertImplicitMultiplication(List<Token> t) {
        if (t.isEmpty())
            return t;
        List<Token> out = new ArrayList<>();
        for (int i = 0; i < t.size(); i++) {
            out.add(t.get(i));
            if (i + 1 < t.size()) {
                Token a = t.get(i), b = t.get(i + 1);
                boolean leftAtomic = a.type == Type.NUMBER || a.type == Type.IDENT || a.type == Type.RPAREN;
                boolean rightAtomic = b.type == Type.NUMBER || b.type == Type.IDENT || b.type == Type.LPAREN;
                if (leftAtomic && rightAtomic)
                    out.add(new Token(Type.OP, "*"));
            }
        }
        return out;
    }

    // shunting-yard
    private static int prec(String op) {
        return switch (op) {
            case "^" -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            default -> -1;
        };
    }

    private static boolean rightAssoc(String op) {
        return "^".equals(op);
    }

    private static List<Token> infixToPostfix(List<Token> tokens) {
        List<Token> out = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();
        for (Token tk : tokens) {
            switch (tk.type) {
                case NUMBER:
                case IDENT:
                    out.add(tk);
                    break;
                case OP:
                    while (!stack.isEmpty() && stack.peek().type == Type.OP) {
                        String top = stack.peek().text;
                        if ((rightAssoc(tk.text) && prec(tk.text) < prec(top)) ||
                                (!rightAssoc(tk.text) && prec(tk.text) <= prec(top))) {
                            out.add(stack.pop());
                        } else
                            break;
                    }
                    stack.push(tk);
                    break;
                case LPAREN:
                    stack.push(tk);
                    break;
                case RPAREN:
                    while (!stack.isEmpty() && stack.peek().type != Type.LPAREN)
                        out.add(stack.pop());
                    if (stack.isEmpty() || stack.pop().type != Type.LPAREN)
                        throw new InvalidEquationException("Mismatched parentheses");
                    break;
            }
        }
        while (!stack.isEmpty()) {
            Token t = stack.pop();
            if (t.type == Type.LPAREN || t.type == Type.RPAREN)
                throw new InvalidEquationException("Mismatched parentheses");
            out.add(t);
        }
        return out;
    }

    // build expression tree from postfix
    private static Node buildTree(List<Token> post) {
        Deque<Node> st = new ArrayDeque<>();
        for (Token tk : post) {
            switch (tk.type) {
                case NUMBER:
                    st.push(new OperandNode(tk.text));
                    break;
                case IDENT:
                    st.push(new OperandNode(tk.text));
                    break;
                case OP:
                    if (st.size() < 2)
                        throw new InvalidEquationException("Missing operands for operator " + tk.text);
                    Node r = st.pop(), l = st.pop();
                    st.push(new OperatorNode(tk.text, l, r));
                    break;
                default:
                    throw new InvalidEquationException("Unexpected token in postfix: " + tk);
            }
        }
        if (st.size() != 1)
            throw new InvalidEquationException("Malformed expression");
        return st.pop();
    }
}
