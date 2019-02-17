package ai.tactics;

import client.model.*;

public class DefenceTactic extends Tactic {
    private Ability toApply;
    public DefenceTactic(Ability ability,Cell aimCell){
        name="DEFENCE";
        this.aimCell=aimCell;
        this.toApply=ability;
    }

    @Override
    public void applyMove(Hero hero, World world) {
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero,toApply,aimCell);
    }
}
