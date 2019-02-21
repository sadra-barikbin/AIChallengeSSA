package ai.tactics;

import ai.common.Functions;
import client.model.*;

import java.util.ArrayList;
import java.util.List;

public class DefenceTactic extends Tactic {
    private Ability toApply;
    public DefenceTactic(Ability ability,Cell aimCell){
        name="DEFENCE";
        this.aimCell=aimCell;
        this.toApply=ability;
    }

    @Override
    public void applyMove(Hero hero, World world) {
        if(world.manhattanDistance(hero.getCurrentCell(),aimCell)<=toApply.getRange())
            return;
        Cell[] liveHeroesPlacesButMe= Functions.getMyLiveHeroesPlacesButMe(world,hero);
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell,liveHeroesPlacesButMe);
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero,toApply,aimCell);
    }
}
