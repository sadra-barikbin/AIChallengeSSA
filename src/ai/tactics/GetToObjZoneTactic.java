package ai.tactics;

import client.model.Cell;

public class GetToObjZoneTactic extends Tactic {

    public GetToObjZoneTactic(Cell aimCell){
        name="GET_TO_OBJ_ZONE";
        this.aimCell=aimCell;
    }

}
