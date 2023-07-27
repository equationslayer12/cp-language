import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;


public class Cp {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: cp <filepath>");
            System.exit(64);
        }
        else if (args.length == 1) {
            String path = args[0];
            runFile(path);
        }
        else {
            runInteractiveMode();
        }

    }

    private static void runInteractiveMode() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("cp> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            /*
            We need to reset the hadError flag in the interactive loop.
            If the user makes a mistake, it shouldn't kill their entire session.
             */
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] sourceBytes = Files.readAllBytes(Paths.get(path));
        String sourceString = new String(sourceBytes, Charset.defaultCharset());
        run(sourceString);
        if (hadError)
            System.exit(65);
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        ArrayList<Token> tokens = lexer.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public static void error(int line, String message) {
        System.err.println("Error [line " + line + "]: " + message);
        hadError = true;
    }
}
