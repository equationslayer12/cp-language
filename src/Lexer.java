import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private final String source;
    private final ArrayList<Token> tokens;
    private int startIndex = 0;
    private int currentIndex = 0;
    private int line = 1;

    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Lexer(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    ArrayList<Token> scanTokens() {
        while (!isAtEnd()) {
            startIndex = currentIndex;
            scanToken();
        }
        return tokens;
    }

    private void scanToken() {
        char current = advance();
        switch (current) {
            case '\t':
            case '\r':
            case ' ':
                break;
            case '\n':
                line++;
                break;
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '/':
                if (nextCharMatches('*'))
                    multilineComment();
                else
                    addToken(TokenType.SLASH);
                break;
            case '#':
                // comment until the end of the line
                while (peek() != '\n' && !isAtEnd())
                    advance();
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                if (nextCharMatches('*'))
                    addToken(TokenType.STAR_STAR);
                else
                    addToken(TokenType.STAR);
                break;
            case '=':
                if (nextCharMatches('='))
                    addToken(TokenType.EQUAL_EQUAL);
                else
                    addToken(TokenType.EQUAL);
                break;
            case '!':
                if (nextCharMatches('='))
                    addToken(TokenType.BANG_EQUAL);
                else
                    addToken(TokenType.BANG);
                break;
            case '<':
                if (nextCharMatches('='))
                    addToken(TokenType.LESS_EQUAL);
                else
                    addToken(TokenType.LESS);
                break;
            case '>':
                if (nextCharMatches('='))
                    addToken(TokenType.GREATER_EQUAL);
                else
                    addToken(TokenType.GREATER);
                break;
            case '\"':
                parseString('"');
                break;
            case '\'':
                parseString('\'');
                break;
            case '.':
                if (isDigit(peek())) {
                    advance();  // consume the '.'
                    parseFloat();
                    break;
                }

            default:
                if (isDigit(current)) {
                    parseNumber();  // could be int or float
                } else if (isAlpha(current)) {
                    parseIdentifier();
                }
                else {
                    Cp.error(line, "unexpected character '" + current + "'");
                }
        }
    }

    private void multilineComment() {
        int startingLine = line;
        while (!(peek() == '*' && peakNext() == '/') && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            Cp.error(startingLine, "Unfinished multiline comment.");
            return;
        }
        advance();  // consume the '*'
        advance();  // consume the '/'
    }

    private boolean isAlpha(char c) {
        return ('a' <= c && c <= 'z') ||
                ('A' <= c && c <= 'Z') ||
                (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void parseIdentifier() {
        while (isAlphaNumeric(peek()))
            advance();
        String lexeme = source.substring(startIndex, currentIndex);
        TokenType type = keywords.get(lexeme);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(currentIndex);
    }

    private char peakNext() {
        if (currentIndex + 1 >= source.length())
            return '\0';
        return source.charAt(currentIndex+1);
    }

    private boolean nextCharMatches(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(currentIndex) != expected)
            return false;

        currentIndex++;
        return true;
    }

    /**
     * parse a string.
     * @param stringChar either ' or "
     */
    private void parseString(char stringChar) {
        while (peek() != stringChar && peek() != '\n' && !isAtEnd())
            advance();

        if (isAtEnd() || peek() == '\n') {
            Cp.error(line, "Unfinished string");
            if (peek() == '\n') {
                advance();
                line++;
            }
            return;
        }

        advance();  // closing "

        //return string without the quotes
        String value = source.substring(startIndex + 1, currentIndex - 1);
        addToken(TokenType.STRING, value);

    }

    private void parseNumber() {
        while (isDigit( peek() ))
            advance();

        if (peek() == '.' && isDigit(peakNext())) {
            advance();  // consume the '.'
            parseFloat();
            return;
        }

        addToken(TokenType.INT, Integer.parseInt(source.substring(startIndex, currentIndex)));
    }

    private void parseFloat() {
        while (isDigit(peek()))
            advance();

        if (peek() == '.') {
            Cp.error(line, "unexpected leading '.' in float");
            return;
        }

        addToken(TokenType.FLOAT, Double.parseDouble(source.substring(startIndex, currentIndex)));
    }

    private boolean isDigit(char character) {
        return '0' <= character && character <= '9';
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(startIndex, currentIndex);
        tokens.add(
                new Token(type, lexeme, literal, line)
        );
    }


    private char advance() {
        return source.charAt(currentIndex++);
    }

    private boolean isAtEnd() {
        return currentIndex >= source.length();
    }

}
