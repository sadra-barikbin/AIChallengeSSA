package ai.tactics;

import client.model.*;

import java.util.ArrayList;
import java.util.List;

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
        Hero[] allHeroes=world.getMyHeroes();
        List<Cell> liveHeroesPlacesButMe=new ArrayList<>();
        for (Hero h:allHeroes){
            if (h.getId()==hero.getId() || h.getCurrentHP()==0)
                continue;
            liveHeroesPlacesButMe.add(h.getCurrentCell());
        }
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell,liveHeroesPlacesButMe.toArray(new Cell[]{}));
        if (path.length!=0)
            world.moveHero(hero,path[0]);
    }

    @Override
    public void applyAction(Hero hero, World world) {

    }
}
