package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;
import util.AVL_tree;

public class DodgeTactic extends Tactic {
    private Cell currentCell;

    @Override
    public void applyMove(Hero hero, World world) {

    }

    @Override
    public void applyAction(Hero hero, World world) {
        int maxJump=hero.getDodgeAbilities()[0].getRange();
        Cell bestOption=null;
        Cell bestBestOption=null;
        for (int dx=-maxJump;dx<=maxJump;dx++){
            for (int dy=-maxJump;dy<=maxJump;dy++){
                if (Math.abs(dx)+Math.abs(dy)>maxJump ||(dx==0 && dy==0))
                    continue;
                int newX=currentCell.getColumn()+dx;
                int newY=currentCell.getRow()+dy;
                if (world.getMap().isInMap(newY,newX) && !world.getMap().getCell(newY,newX).isWall())
                {
                    Cell temp=world.getMap().getCell(newY,newX);
                    if (temp.isInObjectiveZone()) {
                        if (bestBestOption==null ||(world.manhattanDistance(hero.getCurrentCell(),bestBestOption)<world.manhattanDistance(hero.getCurrentCell(),temp)))
                            bestBestOption=temp;
                    }
                    else if (bestOption==null ||(world.manhattanDistance(hero.getCurrentCell(),bestOption)<world.manhattanDistance(hero.getCurrentCell(),temp)) )
                        bestOption=temp;
                }
            }
        }
        if (bestBestOption!=null)
            world.castAbility(hero, hero.getDodgeAbilities()[0], bestBestOption);
        else if (bestOption!=null)
            world.castAbility(hero, hero.getDodgeAbilities()[0], bestOption);

    }

    public DodgeTactic(Cell currentCell){
        name="DODGE";
        this.currentCell=currentCell;
    }
}
