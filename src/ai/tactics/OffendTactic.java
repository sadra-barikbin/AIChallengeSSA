package ai.tactics;

import ai.common.Functions;
import client.model.*;
import util.AVL_tree;

import java.util.ArrayList;
import java.util.List;
import static ai.common.Functions.applyDirectionToCell;
public class OffendTactic extends Tactic {
    private Ability toApply;
    private Direction toGo;
    public OffendTactic(Ability ability,Cell aimCell){
        name="OFFEND";
        this.aimCell=aimCell;
        this.toApply=ability;

    }
    public OffendTactic(Ability ability,Cell aimCell,Direction toGo){
        name="OFFEND";
        this.aimCell=aimCell;
        this.toApply=ability;
        this.toGo=toGo;
    }

    @Override
    public void applyMove(Hero hero, World world) {
        if(world.manhattanDistance(hero.getCurrentCell(),aimCell)<=toApply.getRange()+toApply.getAreaOfEffect()&&(hero.getCurrentCell().isInObjectiveZone()|| !aimCell.isInObjectiveZone()))
            return;
        Direction togo=null;
        if (this.toGo==null) {
            Cell[] liveHeroesPlacesButMe = Functions.getMyLiveHeroesPlacesButMe(world,hero);

            Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell,liveHeroesPlacesButMe);
            if (path.length!=0)togo=path[0];
        }
        else togo=this.toGo;
        if (togo!=null) {
            Cell target=applyDirectionToCell(world,hero.getCurrentCell(),togo);
            if (target.isInObjectiveZone() || aimCell.isInObjectiveZone())
                world.moveHero(hero, togo);
        }
    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero,toApply,aimCell);
    }
}
