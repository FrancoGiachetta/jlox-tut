package jlox;

import java.util.List;

public class LoxClass implements LoxCallable {
    final String name;

    public LoxClass(String name) {
        this.name = name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        return null;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
