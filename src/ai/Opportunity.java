package ai;

import client.model.*;

public class Opportunity implements Comparable<Opportunity>{
    public Cell in;
    public Ability type;
    public boolean isRisky;
    public int at;
    public Hero forr;
    public Hero aimHero;
    public boolean mayNotBeAbleToAttack;
    public Opportunity(Cell in,Ability type,Hero forr,Hero aimHero,int phaseNum,boolean isRisky){
        this.in=in;
        this.type=type;
        this.isRisky=isRisky;
        this.forr=forr;
        this.at=phaseNum;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=false;
    }
    public Opportunity(Cell in, Ability type, Hero forr, Hero aimHero, int phaseNum, boolean isRisky,boolean mayNotBeAbleToAttack){
        this.in=in;
        this.type=type;
        this.isRisky=isRisky;
        this.forr=forr;
        this.at=phaseNum;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=mayNotBeAbleToAttack;
    }
    public Opportunity(Cell in, Ability type, Hero forr ,Hero aimHero,int phaseNum){
        this.in=in;
        this.type=type;
        this.isRisky=false;
        this.forr=forr;
        this.at=phaseNum;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=false;
    }
    public int opportunityDistanceToHero(){
        return Math.abs(forr.getCurrentCell().getRow()-in.getRow())+Math.abs(forr.getCurrentCell().getColumn()-in.getColumn());
    }

    @Override
    public int compareTo(Opportunity o) {
        if (o.type.getName()== AbilityName.HEALER_HEAL)
            return type.getName()==AbilityName.HEALER_HEAL?Integer.compare(o.aimHero.getCurrentHP(),aimHero
            .getCurrentHP()):-1;
        else if(type.getName()==AbilityName.HEALER_HEAL)
            return 1;
        if (type.getName()==AbilityName.GUARDIAN_FORTIFY)
            return 1;
        if (o.type.getName()==AbilityName.GUARDIAN_FORTIFY)
            return -1;
        if (mayNotBeAbleToAttack && !o.mayNotBeAbleToAttack)
            return -1;
        else if (!mayNotBeAbleToAttack && o.mayNotBeAbleToAttack)
            return 1;
        return (type.getPower()>o.type.getPower())?1:(type.getPower()<o.type.getPower())?-1:isRisky?o.isRisky?(Integer.compare(o.aimHero.getCurrentHP(),aimHero.getCurrentHP())):-1:o.isRisky?1:(Integer.compare(o.aimHero.getCurrentHP(),aimHero.getCurrentHP()));
    }
}
