package parser;

import parser.types.Vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class Parser {
    private static final int HEADER_LENGTH = 12;
    private static final int DIRECTORY_ENTRY = 16;
    private final byte[] contents;
    private int numLumps;
    private int directoryLocation;
    private Map<String, Lump> lumps;
    private List<Vertex> vertices;
    public Parser(byte[] contents) {
        this.contents = contents;
        byte[] header = new byte[HEADER_LENGTH];
        System.arraycopy(contents, 0, header, 0, HEADER_LENGTH);
        parseHeader(header);
        lumps = new HashMap<>();
        parseDirectory();
        parseLumps();
    }

    private void parseLumps() {
        parseVertices();
    }

    private void parseSectors() {
        
    }

    private void parseVertices() {
        Lump vertices = lumps.get("VERTEXES");
        this.vertices = new ArrayList<>();
        for (int i = 0; i < vertices.getSize(); i+=4) {
            int curr = vertices.getPos() + i;
            int x = readIntFromBytes(contents, curr, 2);
            int y = readIntFromBytes(contents, curr + 2, 2);
            this.vertices.add(new Vertex(x, y));
        }
    }

    private void parseDirectory() {
        int dirLength = numLumps * 16;
        for (int i = 0; i < dirLength; i+=DIRECTORY_ENTRY) {
            int curr = directoryLocation + i;
            int lumpPos = readIntFromBytes(contents, curr,4);
            int lumpSize = readIntFromBytes(contents, curr + 4, 4);
            String lumpName = readStringFromBytes(contents, curr + 8, 8);
            lumps.put(lumpName.trim(), new Lump(lumpPos, lumpSize, lumpName.trim()));
        }
    }

    private void parseHeader(byte[] header) {
        String id = readStringFromBytes(header, 0, 4);
        if(!id.equals("IWAD")) {
            System.err.println("File format is not correct or unsupported. PWAD files are not supported.");
            System.exit(1);
        }
        numLumps = readIntFromBytes(header, 4, 4);
        directoryLocation = readIntFromBytes(header, 8, 4);
    }

    private int readIntFromBytes(byte[] in, int offset, int length) {
        byte[] out = new byte[length];
        System.arraycopy(in, offset, out, 0, length);
        ByteBuffer buffer = ByteBuffer.wrap(out);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

    private String readStringFromBytes(byte[] in, int offset, int length) {
        return new String(in, offset, length);
    }
}
