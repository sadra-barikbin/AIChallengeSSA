package test;

import ai.AbstractAI;
import client.model.Direction;
import client.model.Hero;
import client.model.HeroName;
import client.model.World;
import static ai.common.Functions.manhatanicNearestCellEmptyOfFriend;
public class TestAI implements AbstractAI {
    @Override
    public void preProcess(World world) {

    }

    @Override
    public void pickTurn(World world) {
        world.pickHero(HeroName.values()[world.getCurrentTurn()]);
    }

    @Override
    public void moveTurn(World world) {
        for (Hero h:world.getMyHeroes()){
            Direction[] path=world.getPathMoveDirections(h.getCurrentCell(),manhatanicNearestCellEmptyOfFriend(world,h,world.getMap().getObjectiveZone(),h.getCurrentCell()));
            if (path.length!=0)
                world.moveHero(h,path[0]);
        }

    }

    @Override
    public void actionTurn(World world) {
        System.out.println(world.getMovePhaseNum());
    }
}
