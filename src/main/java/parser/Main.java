package parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    Parser parser;
    public Main(String wadPath) throws IOException {
        byte[] contents = Files.readAllBytes(new File(wadPath).toPath());
        parser = new Parser(contents);
    }

    public static void main(String[] args) throws IOException {
        new Main(args[0]);
    }
}
