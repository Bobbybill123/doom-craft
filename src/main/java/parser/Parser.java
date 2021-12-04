package parser;

import parser.types.LineDef;
import parser.types.Sector;
import parser.types.SideDef;
import parser.types.Vertex;

import java.nio.BufferUnderflowException;
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
    private Map<Integer, Sector> sectors;
    private List<SideDef> sideDefs;
    private List<LineDef> lineDefs;
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
        parseSectors();
        parseSideDefs();
        parseLineDefs();
    }

    private void parseLineDefs() {
        Lump linedefLump = lumps.get("LINEDEFS");
        lineDefs = new ArrayList<>();
        for (int i = 0; i < linedefLump.getSize(); i+=14) {
            int curr = linedefLump.getPos() + i;
            int startVertex = readIntFromBytes(contents, curr, 2);
            int endVertex = readIntFromBytes(contents, curr+=2, 2);
            byte[] flags = new byte[2];
            System.arraycopy(contents, curr+=2, flags, 0, 2);
            int type = readIntFromBytes(contents, curr+=2, 2);
            int sectorTag = readIntFromBytes(contents, curr+=2, 2);
            int frontSidedef = readIntFromBytes(contents, curr+=2, 2);
            int endSidedef = readIntFromBytes(contents, curr, 2);
            Vertex start = vertices.get(startVertex);
            Vertex end = vertices.get(endVertex);
            Sector sector = sectors.get(sectorTag);
            SideDef front = sideDefs.get(frontSidedef);
            SideDef back = sideDefs.get(endSidedef);
            lineDefs.add(new LineDef(start, end, flags, type, sector, front, back));
        }
    }

    private void parseSideDefs() {
        Lump sidedefLump = lumps.get("SIDEDEFS");
        sideDefs = new ArrayList<>();
        for (int i = 0; i < sidedefLump.getSize(); i+=30) {
            int curr = sidedefLump.getPos() + i;
            int xOffset = readIntFromBytes(contents, curr, 2);
            int yOffset = readIntFromBytes(contents, curr+=2, 2);
            String upperName = readStringFromBytes(contents, curr+=8, 8);
            String lowerName = readStringFromBytes(contents, curr+=8, 8);
            String midName = readStringFromBytes(contents, curr+=8, 8);
            int sectorNum = readIntFromBytes(contents, curr, 2);
            sideDefs.add(new SideDef(xOffset, yOffset, upperName, lowerName, midName, sectors.get(sectorNum)));
        }
    }

    private void parseSectors() {
        Lump sectorLump = lumps.get("SECTORS");
        sectors = new HashMap<>();
        for (int i = 0; i < sectorLump.getSize(); i+=26) {
            int curr = sectorLump.getPos() + i;
            int floorHeight = readIntFromBytes(contents, curr, 2);
            int ceilHeight = readIntFromBytes(contents, curr+=2, 2);
            String floorTexName = readStringFromBytes(contents, curr+=8, 8);
            String ceilTexName = readStringFromBytes(contents, curr+=8, 8);
            int lightLevel = readIntFromBytes(contents, curr+=2, 2);
            int specialType = readIntFromBytes(contents, curr+=2, 2);
            int tagNum = readIntFromBytes(contents, curr, 2);
            sectors.put(tagNum, new Sector(floorHeight, ceilHeight, floorTexName, ceilTexName, lightLevel, specialType, tagNum));
        }
    }

    private void parseVertices() {
        Lump vertexLump = lumps.get("VERTEXES");
        vertices = new ArrayList<>();
        for (int i = 0; i < vertexLump.getSize(); i+=4) {
            int curr = vertexLump.getPos() + i;
            int x = readIntFromBytes(contents, curr, 2);
            int y = readIntFromBytes(contents, curr + 2, 2);
            vertices.add(new Vertex(x, y));
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
            System.err.println("File format is not correct or unsupported.");
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
        return length == 4 ? buffer.getInt() : buffer.getShort();
    }

    private String readStringFromBytes(byte[] in, int offset, int length) {
        return new String(in, offset, length);
    }
}
