package jlox;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token op = previous();
            Expr rhs = comparison();

            expr = new Expr.Binary(expr, op, rhs);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER_EQUAL, TokenType.GREATER, TokenType.LESS_EQUAL, TokenType.LESS)) {
            Token op = previous();
            Expr rhs = term();

            expr = new Expr.Binary(expr, op, rhs);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Expr rhs = factor();

            expr = new Expr.Binary(expr, op, rhs);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token op = previous();
            Expr rhs = unary();

            expr = new Expr.Binary(expr, op, rhs);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token op = previous();
            Expr rhs = unary();

            return new Expr.Unary(op, rhs);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.TRUE))
            return new Expr.Literal(true);
        if (match(TokenType.FALSE))
            return new Expr.Literal(false);
        if (match(TokenType.NIL))
            return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(previous().literal);

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
    }

    private boolean match(TokenType... tokens) {
        for (TokenType token : tokens) {
            if (check(token)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType token) {
        if (isAtEnd())
            return false;
        return peek().type == token;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token peek() {
        return tokens.get(current);
    }
}
