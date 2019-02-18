package ai.tactics;

import client.model.*;

public class OffendTactic extends Tactic {
    private Ability toApply;
    public OffendTactic(Ability ability,Cell aimCell){
        name="OFFEND";
        this.aimCell=aimCell;
        this.toApply=ability;
    }

    @Override
    public void applyMove(Hero hero, World world) {
        if(world.manhattanDistance(hero.getCurrentCell(),aimCell)<=toApply.getRange())
            return;
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero,toApply,aimCell);
    }
}
