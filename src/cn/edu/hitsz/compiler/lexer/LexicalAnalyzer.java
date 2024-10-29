package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private final List<Character> charBuffer = new ArrayList<>();
    private final List<Token> tokens = new ArrayList<>();
    private StringBuilder sb = new StringBuilder();
    private int condition = 0;
    private int lastCondition = 0;

    private static final Set<Character> OPERATORS = Set.of('=', ',', '+', '-', '*', '/', '(', ')');

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void loadFile(String path) {
        try (FileReader fileReader = new FileReader(path)) {
            int character;
            while ((character = fileReader.read()) != -1) {
                charBuffer.add((char) character);
            }
        } catch (IOException e) {
            // Use logging in production code
            e.printStackTrace();
        }
    }

    private Token createSimpleToken(char input) {
        return Token.simple(String.valueOf(input));
    }

    public Token getNextCondition(char input) {
        boolean isAlphanumeric = Character.isDigit(input) || Character.isLetter(input);
        if (isAlphanumeric) sb.append(input);

        switch (condition) {
            case 0:
                if (Character.isWhitespace(input)) return null;
                if (OPERATORS.contains(input)) return Token.simple(String.valueOf(input));
                if (input == ';') return Token.simple("Semicolon");
                if (input == 'i') { condition = 1; return null; }
                if (input == 'r') { condition = 3; return null; }
                if (Character.isDigit(input)) { condition = 52; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 1:
                if (input == 'n') { condition = 2; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 2:
                if (input == 't') { condition = 8; lastCondition = 2; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 3:
                if (input == 'e') { condition = 4; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 4:
                if (input == 't') { condition = 5; lastCondition = 4; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 5:
                if (input == 'u') { condition = 6; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 6:
                if (input == 'r') { condition = 7; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 7:
                if (input == 'n') { condition = 8; lastCondition = 7; return null; }
                if (isAlphanumeric) { condition = 51; return null; }
                break;

            case 8:
                if (input == ';') condition = 10;
                else if (Character.isWhitespace(input)) condition = 0;
                else if (isAlphanumeric) { condition = 51; return null; }

                if (lastCondition == 2) return Token.simple("int");
                if (lastCondition == 7) return Token.simple("return");
                break;

            case 51:
                if (isAlphanumeric) return null;
                if (Character.isWhitespace(input)) { condition = 0; return Token.normal("id", sb.toString()); }
                if (input == ';') { condition = 10; return Token.normal("id", sb.toString()); }
                break;

            case 52:
                if (Character.isDigit(input)) return null;
                if (Character.isWhitespace(input)) { condition = 0; return Token.normal("IntConst", sb.toString()); }
                if (input == ';') { condition = 10; return Token.normal("IntConst", sb.toString()); }
                break;
        }

        return null;
    }

    public void run() {
        condition = 0;
        for (char c : charBuffer) {
            Token token = getNextCondition(c);
            if (token != null) {
                tokens.add(token);
                if (token.getKind().getCode() == 51) {
                    symbolTable.add(sb.toString());
                }
                sb.setLength(0);
            }
            if (condition == 10) {
                tokens.add(Token.simple("Semicolon"));
                sb.setLength(0);
                condition = 0;
            }
        }
        tokens.add(Token.simple("$"));
    }

    public Iterable<Token> getTokens() {
        return tokens;
    }



    public void dumpTokens(String path) {
            FileUtils.writeLines(
                path,
                StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
            );
        }


}
