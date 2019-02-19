package ai.tactics;

import ai.ComplexAI;
import client.model.Cell;
import client.model.Direction;
import client.model.Hero;
import client.model.World;

import java.util.ArrayList;
import java.util.List;

public class GetToObjZoneTactic extends Tactic {
    @Override
    public void applyAction(Hero hero, World world) {

    }

    @Override
    public void applyMove(Hero hero, World world) {
        List<Cell> heroesButThis=new ArrayList<>();
        for (Hero friend:world.getMyHeroes()){
            if (friend.getCurrentHP()==0 || friend.getId()==hero.getId())
                continue;
            heroesButThis.add(friend.getCurrentCell());
        }
        Direction[] goodPath=world.getPathMoveDirections(hero.getCurrentCell(),aimCell,heroesButThis);
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
