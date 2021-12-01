package parser.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SideDef {
    private int xOffset;
    private int yOffset;
    private String upperName;
    private String lowerName;
    private String middleName;
}
