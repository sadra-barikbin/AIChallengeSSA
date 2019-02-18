package ai.tactics;

import client.model.*;
import util.AVL_tree;

public class OffendTactic extends Tactic {
    private Ability toApply;
    private AVL_tree<Cell> objZone;
    public OffendTactic(Ability ability,Cell aimCell, AVL_tree<Cell> objZone){
        name="OFFEND";
        this.aimCell=aimCell;
        this.toApply=ability;
        this.objZone=objZone;
    }

    @Override
    public void applyMove(Hero hero, World world) {
        if(world.manhattanDistance(hero.getCurrentCell(),aimCell)<=toApply.getRange())
            return;
        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),aimCell);
        if (path.length!=0) {
            Cell target;
            switch (path[0]){
                case UP:{
                    target=world.getMap().getCell(hero.getCurrentCell().getRow()-1,hero.getCurrentCell().getColumn());
                    break;
                }
                case DOWN:{
                    target=world.getMap().getCell(hero.getCurrentCell().getRow()+1,hero.getCurrentCell().getColumn());
                    break;
                }
                case LEFT:{
                    target=world.getMap().getCell(hero.getCurrentCell().getRow(),hero.getCurrentCell().getColumn()-1);
                    break;
                }
                case RIGHT:{
                    target=world.getMap().getCell(hero.getCurrentCell().getRow(),hero.getCurrentCell().getColumn()+1);
                    break;
                }
                default:{
                    target=hero.getCurrentCell();
                    break;
                }
            }
            if (objZone.exist(target))
                world.moveHero(hero, path[0]);
        }
    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero,toApply,aimCell);
    }
}
