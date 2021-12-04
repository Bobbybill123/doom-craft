package parser.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineDef {
    private Vertex start, end;
    private byte[] flags;
    int type;
    Sector sector;
    SideDef front, back;
}
