package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class lox {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            // If a user adds multiple script names (example), error is thrown back showing them how to use the script
            System.out.println("Usage: jlox [script]");
            System.exit(0);
        } else if (args.length == 1) {
            // If exactly one file is given, run that file
            runFile(args[0]);
        } else {
            // If no files are given, enter the REPL
            runPrompt();
        }
    }

    // Scanner - turns the given file into tokens and runs it through lox
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // IF there's an error, immediately quit the program and don't run anymore code
        if (hadError) {
            System.exit(65);
        }
    }

    // Creates a REPL for you to run individual lox files if no argument/file is provided
    private static void runPrompt() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            try {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                run(line);
                // Even if there is an error with the line, it shouldn't kill the whole REPL, so we reset the boolean
                hadError = false;
            } catch (IOException e) {
                System.out.println("Error reaing line: " + e);
            }
        }
    }

    private static void run(String source) {
        scanner scanner = new scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message
        );
        hadError = true;
    }
}