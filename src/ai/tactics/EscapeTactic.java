package ai.tactics;

import client.model.Cell;
import client.model.Direction;
import client.model.Hero;
import client.model.World;

import java.util.ArrayList;
import java.util.List;

public class EscapeTactic extends Tactic {
    @Override
    public void applyMove(Hero hero, World world) {
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

    public EscapeTactic(Cell aimCell){
        name="ESCAPE";
        this.aimCell=aimCell;
    }
}
