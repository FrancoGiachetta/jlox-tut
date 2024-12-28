package jlox;

import jlox.Expr.Binary;
import jlox.Expr.Grouping;
import jlox.Expr.Literal;
import jlox.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {
    public void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    private String stringify(Object obj) {
        if (obj == null)
            return "nil";
        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }

        return obj.toString();
    }

    @Override
    public Object visitLiteralExpr(Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Grouping groupOp) {
        return evaluate(groupOp.expression);
    }

    @Override
    public Object visitUnaryExpr(Unary unaryOp) {
        Object rhs = evaluate(unaryOp.right);

        switch (unaryOp.op.type) {
            case TokenType.MINUS:
                checkNumberOperand(unaryOp.op, rhs);
                return -(double) rhs;
            case TokenType.BANG:
                return !isTruthy(rhs);
        }

        // Should be unreachable
        return null;
    }

    private void checkNumberOperand(Token op, Object operand) {
        if (operand instanceof Double)
            return;

        throw new RuntimeError(op, "Operand must be a number");
    }

    @Override
    public Object visitBinaryExpr(Binary binaryOp) {
        Object lhs = evaluate(binaryOp.left);
        Object rhs = evaluate(binaryOp.right);

        switch (binaryOp.op.type) {
            case TokenType.GREATER:
                checkNumberOperands(binaryOp.op, lhs, rhs);
                return (double) lhs > (double) rhs;
            case TokenType.GREATER_EQUAL:
                return (double) lhs >= (double) rhs;
            case TokenType.LESS:
                return (double) lhs < (double) rhs;
            case TokenType.LESS_EQUAL:
                return (double) lhs <= (double) rhs;
            case TokenType.BANG_EQUAL:
                return !isEqual(lhs, rhs);
            case TokenType.EQUAL_EQUAL:
                return isEqual(lhs, rhs);
            case TokenType.PLUS: {
                if (lhs instanceof Double && rhs instanceof Double)
                    return (double) lhs + (double) rhs;
                if (lhs instanceof String && rhs instanceof String)
                    return (String) lhs + (String) rhs;

                throw new RuntimeError(binaryOp.op, "Operands must be two numbers or two strings");
            }
            case TokenType.MINUS:
                return (double) lhs - (double) rhs;
            case TokenType.STAR:
                return (double) lhs * (double) rhs;
            case TokenType.SLASH:
                return (double) lhs / (double) rhs;
        }

        // Should be unreachable
        return null;
    }

    private void checkNumberOperands(Token op, Object lhs, Object rhs) {
        if (lhs instanceof Double && lhs instanceof Double)
            return;

        throw new RuntimeError(op, "Operands must be two numbers");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Boolean)
            return (boolean) obj;

        return true;
    }

    private boolean isEqual(Object lhs, Object rhs) {
        if (lhs == null && rhs == null)
            return true;
        if (lhs == null)
            return false;

        return lhs.equals(rhs);
    }
}
