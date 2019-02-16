package ai.tactics;

import client.model.Cell;

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
}
