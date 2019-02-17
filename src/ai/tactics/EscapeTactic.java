package ai.tactics;

import client.model.Cell;
import client.model.Direction;
import client.model.Hero;
import client.model.World;

public class EscapeTactic extends Tactic {
    @Override
    public void applyMove(Hero hero, World world) {
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {

    }

    public EscapeTactic(Cell aimCell){
        name="ESCAPE";
        this.aimCell=aimCell;
    }
}
