package ai.tactics;

import client.model.Cell;

public class HoldOnTactic extends Tactic {
    public HoldOnTactic(Cell aimCell){
        name="HOLD_ON";
        this.aimCell=aimCell;
    }
}
