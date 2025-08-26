package com.algebra.algebra_solver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

public class Equation {
    private String id;
    private String infix; // human-friendly infix reconstructed from tree
    @JsonIgnore
    private Node root; // expression tree (postfix tree)
    private Set<String> variables;

    public Equation() {
    }

    public Equation(String id, String infix, Node root, Set<String> variables) {
        this.id = id;
        this.infix = infix;
        this.root = root;
        this.variables = variables;
    }

    public String getId() {
        return id;
    }

    public String getInfix() {
        return infix;
    }

    @JsonIgnore
    public Node getRoot() {
        return root;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void setVariables(Set<String> variables) {
        this.variables = variables;
    }
}
