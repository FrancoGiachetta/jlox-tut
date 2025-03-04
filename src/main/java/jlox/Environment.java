package jlox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public Object assignAt(int distance, Token name, Object value) {
        return ancestor(distance).values.put(name.lexeme, value);
    }

    private Environment ancestor(int distance) {
        Environment env = this;

        for (int i = 0; i < distance; i++) {
            env = env.enclosing;
        }

        return env;
    }

    public void assign(Token token, Object value) {
        if (values.containsKey(token.lexeme)) {
            values.put(token.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(token, value);
            return;
        }

        throw new RuntimeError(token, "Undefined variable '" + token.lexeme + "'.");
    }

    public Object get(Token token) {
        if (values.containsKey(token.lexeme))
            return values.get(token.lexeme);

        if (enclosing != null)
            return enclosing.get(token);

        throw new RuntimeError(token, "Undefined variable '" + token.lexeme + "'.");
    }
}
