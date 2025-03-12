package jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Environment closure;
    private final Stmt.Function declaration;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
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
            if (isInitializer)
                return closure.getAt(0, "this");
            return returnVale.value;
        }

        if (isInitializer)
            return closure.getAt(0, "this");

        return null;
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }

    @Override
    public String toString() {
        return "<fn" + declaration.name.lexeme + ">";
    }
}
