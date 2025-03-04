package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jlox.Expr.Assign;
import jlox.Expr.Binary;
import jlox.Expr.Call;
import jlox.Expr.Get;
import jlox.Expr.Grouping;
import jlox.Expr.Literal;
import jlox.Expr.Logical;
import jlox.Expr.Set;
import jlox.Expr.Unary;
import jlox.Expr.Variable;
import jlox.Stmt.Block;
import jlox.Stmt.Class;
import jlox.Stmt.Function;
import jlox.Stmt.If;
import jlox.Stmt.Print;
import jlox.Stmt.Return;
import jlox.Stmt.Var;
import jlox.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public Environment global = new Environment();
    private Environment env = global;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter() {
        global.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
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
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(env));
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        env.define(stmt.name.lexeme, null);
        LoxClass klass = new LoxClass(stmt.name.lexeme);
        env.assign(stmt.name, klass);
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment env) {
        Environment previous = this.env;

        try {
            this.env = env;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.env = previous;
        }
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        env.define(stmt.name.lexeme, value);

        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition)))
            execute(stmt.body);
        return null;
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);

        if (distance != null)
            env.assignAt(distance, expr.name, value);
        else
            global.assign(expr.name, value);

        return value;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        LoxCallable function = new LoxFunction(stmt, this.env);
        env.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new jlox.Return(value);
    }

    @Override
    public Object visitLiteralExpr(Literal literal) {
        return literal.value;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.op.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance))
            throw new RuntimeError(expr.name, "Only instances have fields.");

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        
        return value;
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

    @Override
    public Object visitVariableExpr(Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);

        if (distance != null) {
            return env.getAt(distance, name.lexeme);
        } else {
            return global.get(name);
        }
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
                return (double) lhs < (double) rhs;
            case TokenType.GREATER_EQUAL:
                return (double) lhs <= (double) rhs;
            case TokenType.LESS:
                return (double) lhs > (double) rhs;
            case TokenType.LESS_EQUAL:
                return (double) lhs >= (double) rhs;
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

    @Override
    public Object visitCallExpr(Call expr) {
        Object calle = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();

        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(calle instanceof LoxCallable))
            throw new RuntimeError(expr.paren, "Can only call functions and classes");

        LoxCallable function = (LoxCallable) calle;

        if (arguments.size() != function.arity())
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Get expr) {
        Object object = evaluate(expr);
        if (object instanceof LoxInstance)
            return ((LoxInstance) object).get(expr.name);

        throw new RuntimeError(expr.name, "Only instances have properties");
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
