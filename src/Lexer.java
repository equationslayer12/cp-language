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
            case '.':
                addToken(TokenType.DOT);
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

            default:
                Cp.error(line, "unexpected character '" + current + "'");
        }
    }

    private char peak() {
        if (isAtEnd())
            return '\0';
        return source.charAt(currentIndex);
    }

    private boolean nextCharMatches(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(currentIndex) != expected)
            return false;

        currentIndex++;
        return true;
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
