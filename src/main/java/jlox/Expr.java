package jlox;

import java.util.List;

abstract class Expr{
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
    }
    public static class Binary extends Expr {
        final Expr left;
        final Token op;
        final Expr right;
        public Binary(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }
    public static class Grouping extends Expr {
        final Expr expression;
        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }
    public static class Literal extends Expr {
        final Object value;
        public Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }
    public static class Unary extends Expr {
        final Token op;
        final Expr right;
        public Unary(Token op, Expr right) {
            this.op = op;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    abstract<R> R accept(Visitor<R> visitor);
}
