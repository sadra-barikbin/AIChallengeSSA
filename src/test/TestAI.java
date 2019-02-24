package test;

import ai.AbstractAI;
import client.model.*;

import java.util.ArrayList;
import java.util.List;

import static ai.common.Functions.manhatanicNearestCellEmptyOfFriend;
public class TestAI implements AbstractAI {
    List<Cell> fourCells;
    @Override
    public void preProcess(World world) {
        fourCells=new ArrayList<>();
        fourCells.add(world.getMap().getCell(16,12));
        fourCells.add(world.getMap().getCell(15,14));
        fourCells.add(world.getMap().getCell(13,12));
        fourCells.add(world.getMap().getCell(10,4));
    }

    @Override
    public void pickTurn(World world) {
            world.pickHero(HeroName.values()[world.getCurrentTurn()]);
    }

    @Override
    public void moveTurn(World world) {
        for (int h=0;h<4;h++){
            Direction[] path=world.getPathMoveDirections(world.getMyHeroes()[h].getCurrentCell(),fourCells.get(h));
            if (path.length!=0)
                world.moveHero(world.getMyHeroes()[h],path[0]);
        }
    }

    @Override
    public void actionTurn(World world) {

    }
}
