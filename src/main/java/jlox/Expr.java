package jlox;

import java.util.List;

abstract class Expr{
    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitSetExpr(Set expr);
        R visitThisExpr(This expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitVariableExpr(Variable expr);
        R visitUnaryExpr(Unary expr);
    }
    public static class Assign extends Expr {
        final Token name;
        final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
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
    public static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }
    public static class Get extends Expr {
        final Expr object;
        final Token name;
        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }
    public static class Set extends Expr {
        final Expr object;
        final Token name;
        final Expr value;
        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }
    public static class This extends Expr {
        final Token keyword;
        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
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
    public static class Logical extends Expr {
        final Expr left;
        final Token op;
        final Expr right;
        public Logical(Expr left, Token op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }
    public static class Variable extends Expr {
        final Token name;
        public Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
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
