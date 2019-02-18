package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;

public class DodgeTactic extends Tactic {
    private Cell currentCell;

    @Override
    public void applyMove(Hero hero, World world) {

    }

    @Override
    public void applyAction(Hero hero, World world) {

        for (int dx=-1;dx<=1;dx++){
            for (int dy=-1;dy<=1;dy++){
                int newX=currentCell.getColumn()+dx;
                int newY=currentCell.getRow()+dy;
                if (world.getMap().isInMap(newY,newX) && !world.getMap().getCell(newY,newX).isWall())
                {
                    System.out.println("dodge done!");
                    world.castAbility(hero,hero.getDodgeAbilities()[0],newY,newX);
                    return;
                }
            }
        }
    }

    public DodgeTactic(Cell currentCell){
        name="DODGE";
        this.currentCell=currentCell;
    }
}
