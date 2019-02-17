package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;

public abstract class Tactic {
    protected String name;
    protected Cell aimCell;
    @Override
    public boolean equals(Object obj) {
        if (!( obj instanceof String))
            return super.equals(obj);
        else return name.equals(obj);
    }
    public void setAimCell(Cell c){
        aimCell=c;
    }
    public Cell getAimCell(){
        return aimCell;
    }
    public abstract void applyMove(Hero hero, World world);
    public abstract void applyAction(Hero hero,World world);
}
