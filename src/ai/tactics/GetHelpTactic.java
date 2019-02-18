package ai.tactics;

import client.model.*;

public class GetHelpTactic extends Tactic {
    private Ability toGet;
    public GetHelpTactic(Ability ability,Cell aimCell){
        name="GET_HELP";
        this.aimCell=aimCell;
        this.toGet=ability;
    }
    @Override
    public void applyMove(Hero hero, World world) {
        if(world.manhattanDistance(hero.getCurrentCell(),aimCell)<=toGet.getRange())
            return;
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {

    }
}
