package jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Environment closure;
    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++)
            env.define(declaration.params.get(i).lexeme, arguments.get(i));

        try {
            interpreter.executeBlock(declaration.body, env);
        } catch (Return returnVale) {
            return returnVale.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn" + declaration.name.lexeme + ">";
    }
}
