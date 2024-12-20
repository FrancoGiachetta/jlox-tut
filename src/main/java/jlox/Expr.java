package jlox;

import java.util.List;

abstract class Expr{
    public static class Binary extends Expr {
        final Expr left;
        final Token op;
        final Expr right;
        public Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
    }
    public static class Grouping extends Expr {
        final Expr expression;
        public Grouping(Expr expression) {
            this.expression = expression;
        }
    }
    public static class Literal extends Expr {
        final Object value;
        public Literal(Object value) {
            this.value = value;
        }
    }
    public static class Unary extends Expr {
        final Token op;
        final Expr right;
        public Unary(Token op, Expr right) {
            this.op = op;
            this.right = right;
        }
    }
}
