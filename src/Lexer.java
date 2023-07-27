import java.util.ArrayList;

public class Lexer {
    private final String source;
    private final ArrayList<Token> tokens;
    private int startIndex = 0;
    private int currentIndex = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
        this.tokens = new ArrayList<Token>();
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
            case '#':
                // comment until the end of the line
                while (peak() != '\n' && !isAtEnd())
                    advance();
                break;
            case '\"':
                parseString('"');
                break;
            case '\'':
                parseString('\'');
                break;
            case '.':
                if (isDigit(peak())) {
                    advance();  // consume the '.'
                    parseFloat();
                    break;
                }

            default:
                if (isDigit(current)) {
                    parseNumber();  // could be int or float
                    break;
                }
                Cp.error(line, "unexpected character '" + current + "'");
        }
    }

    private char peak() {
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
        while (peak() != stringChar && peak() != '\n' && !isAtEnd())
            advance();

        if (isAtEnd() || peak() == '\n') {
            Cp.error(line, "Unfinished string");
            if (peak() == '\n') {
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
        while (isDigit( peak() ))
            advance();

        if (peak() == '.' && isDigit(peakNext())) {
            advance();  // consume the '.'
            parseFloat();
            return;
        }

        addToken(TokenType.INT, Integer.parseInt(source.substring(startIndex, currentIndex)));
    }

    private void parseFloat() {
        while (isDigit(peak()))
            advance();

        if (peak() == '.') {
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
