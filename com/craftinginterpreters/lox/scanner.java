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
    private static final Map<String, TokenType> keywords;

    static {
        // generating a new hashmap containing all of the keywords and token names
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

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
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if(match('/')) {
                    // if we have 2 slashes in a row, then we can ignore the whole line because it's a comment (treating it as a singular token and then doing nothing with it)
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            // ' ', \r, and \t are all whitespace characters that we can ignore because they aren't tokens
            case ' ':
                break;
            case '\r':
                break;
            case '\t':
                break;
            case '"': string(); break;
            case 'o':
                if(match('r')) {
                    addToken(OR);
                }
                break;
            
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) { // this goes for any alphabetical character or underscore
                    identifier(); // if we encounter a keyword, it could actually be the start of an identifier, so we need to check (eg. 'or' keyword vs 'orchid' variable identifier)
                }  else {
                    lox.error(line, "unexpected character");
                }
                // if a character is passed in that lox doesn't recognize (#, @, etc.), we throw an error instead of silently discarding them
                break;
        }
    }

    //------HELPER FUNCTIONS------

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);

        addToken(IDENTIFIER);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // lookahead method that looks at the next character (good for going thru comments)
    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // peeks 2 characters ahead in the case of decimal points - if there's nothing after the decimal point, we don't want to consume the token
    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

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

    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        // if we reach the end of the line without terminating the string, throw an error
        if(isAtEnd()) {
            lox.error(line, "unterminated string");
            return;
        }

        // if we have a teriminating double quote, we advance
        advance();

        // trim the surrounding quotes to just pull the content
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
}
