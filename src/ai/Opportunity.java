package ai;

import client.model.*;

public class Opportunity implements Comparable<Opportunity>{
    public Cell in;
    public Ability type;
    private boolean isRisky;
    private Phase at;
    public Hero forr;
    public Hero aimHero;
    private boolean mayNotBeAbleToAttack;
    private int targetsCount;
    private int targetsMeanHealth;
    public Opportunity(Cell in,Ability type,Hero forr,Hero aimHero,Phase phase,boolean isRisky){
        this.in=in;
        this.type=type;
        this.isRisky=isRisky;
        this.forr=forr;
        this.at=phase;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=false;
        this.targetsCount=1;
        targetsMeanHealth=aimHero.getCurrentHP();
    }
    public Opportunity(Cell in, Ability type, Hero forr, Hero aimHero, Phase phase, boolean isRisky,boolean mayNotBeAbleToAttack){
        this.in=in;
        this.type=type;
        this.isRisky=isRisky;
        this.forr=forr;
        this.at=phase;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=mayNotBeAbleToAttack;
        this.targetsCount=1;
        targetsMeanHealth=aimHero.getCurrentHP();
    }
    public Opportunity(Cell in, Ability type, Hero forr ,Hero aimHero,Phase phase){
        this.in=in;
        this.type=type;
        this.isRisky=false;
        this.forr=forr;
        this.at=phase;
        this.aimHero=aimHero;
        this.mayNotBeAbleToAttack=false;
        this.targetsCount=1;
        targetsMeanHealth=aimHero.getCurrentHP();
    }
    public Opportunity(Cell in,Ability type,Hero forr,Phase phase,int targetsCount,int targetsMeanHealth){
        this.in=in;
        this.type=type;
        this.forr=forr;
        this.at=phase;
        this.mayNotBeAbleToAttack=false;
        this.targetsCount=targetsCount;
        this.targetsMeanHealth=targetsMeanHealth;
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
        if (at==Phase.MOVE)
            return (type.getPower()>o.type.getPower())?1:(type.getPower()<o.type.getPower())?-1:isRisky?o.isRisky?(Integer.compare(o.targetsMeanHealth,targetsMeanHealth)):-1:o.isRisky?1:(Integer.compare(o.targetsMeanHealth,targetsMeanHealth));
        else
            return (targetsCount*type.getPower()>o.targetsCount*o.type.getPower())?1:(targetsCount*type.getPower()<o.targetsCount*o.type.getPower())?-1:Integer.compare(o.targetsMeanHealth,targetsMeanHealth);
    }
}
