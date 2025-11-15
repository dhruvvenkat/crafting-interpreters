package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    scanner (String source) {
        this.source = source;
    }

    // function that generates a list of all of the tokens we're going to scan
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            //beginning of the next lexeme
            start = current;
            scanToken();
        }

        // once we've reached the list character in the file, we add an end-of-file token to the end to make things cleaner
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    // function that recognizes/parses actual tokens
    private void scanToken() {
        char c = advance();
        switch(c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
        }
    }

    //------HELPER FUNCTIONS------
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // helper method to go from literal to literal
    private char advance() {
        return source.charAt(current++);
    }

    // addToken for tokens that don't need a literal (single-character tokens like ., +, etc)
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // addToken for tokens that need literals
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
