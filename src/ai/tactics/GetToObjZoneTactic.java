package ai.tactics;

import client.model.Cell;
import client.model.Direction;
import client.model.Hero;
import client.model.World;

public class GetToObjZoneTactic extends Tactic {
    @Override
    public void applyAction(Hero hero, World world) {

    }

    @Override
    public void applyMove(Hero hero, World world) {
        Direction[] goodPath=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (goodPath.length!=0)
        {
            Direction goodDir=goodPath[0];
            world.moveHero(hero, goodDir);
        }
    }

    public GetToObjZoneTactic(Cell aimCell){
        name="GET_TO_OBJ_ZONE";
        this.aimCell=aimCell;
    }

}
