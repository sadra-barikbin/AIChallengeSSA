package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;

public class HoldOnTactic extends Tactic {
    @Override
    public void applyMove(Hero hero, World world) {

    }

    @Override
    public void applyAction(Hero hero, World world) {

    }

    public HoldOnTactic(Cell aimCell){
        name="HOLD_ON";
        this.aimCell=aimCell;
    }
}
