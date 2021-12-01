package parser.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sector {
    private int floorHeight;
    private int ceilingHeight;
    private String floorName;
    private String ceilingName;
    private int lightLevel;
    private int specialType;
    private int tagNumber;
}
