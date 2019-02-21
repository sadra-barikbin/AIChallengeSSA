package ai;

import client.model.Ability;
import client.model.Cell;
import client.model.Hero;

public class Danger implements Comparable<Danger>{
    public Hero forr;//een field faghat too ye noubat motabare
    public Cell from;
    public Ability type;
    public Danger(Cell from,Ability type,Hero forr){
        this.from=from;
        this.type=type;
        this.forr=forr;
    }
    public int dangerDistanceToHero(){
        return Math.abs(forr.getCurrentCell().getRow()-from.getRow())+Math.abs(forr.getCurrentCell().getColumn()-from.getColumn());
    }

    @Override
    public int compareTo(Danger o) {
        return Integer.compare(type.getPower(),o.type.getPower());
    }
}
