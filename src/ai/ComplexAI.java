package ai;

import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.*;

/**
made by Sadra
 **/
public class ComplexAI implements AbstractAI {
    private Random random = new Random();
    private AVL_tree<Cell> walls;
    private AVL_tree<Cell> objectiveCells;
    private java.util.Map<Integer,Tactic> heroesTactics;
    @Override
    public void preProcess(World world) {
        System.out.println("preProcess in complexAI started");
        Cell[][] map=world.getMap().getCells();
        List<Cell> wallOnes=new ArrayList<>();
        for (Cell[] row:map){
            for (Cell one:row){
                if (one.isWall())
                    wallOnes.add(one);
            }
        }
        walls=new AVL_tree<>(wallOnes.toArray(new Cell[]{}));
        objectiveCells=new AVL_tree<>(world.getMap().getObjectiveZone());
    }
    private int numOfMyBlasters=0;
    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        if (world.getCurrentTurn()==0) {
            world.pickHero(HeroName.BLASTER);
            numOfMyBlasters++;
        }
        else if (world.getCurrentTurn()==1){
            world.pickHero(HeroName.HEALER);
        }
        else if(world.getCurrentTurn()==2){
            if (world.getOppHeroes()[0].getName()==HeroName.BLASTER&&world.getOppHeroes()[1].getName()==HeroName.BLASTER) {
                world.pickHero(HeroName.BLASTER);
                numOfMyBlasters++;
            }
            else {
                world.pickHero(HeroName.SENTRY);
            }
        }
        else if (world.getCurrentTurn()==3){
            int numOfBlasters=0;
            for (Hero enemy:world.getOppHeroes()){
                if (enemy.getName()==HeroName.BLASTER)
                    numOfBlasters++;
            }
            if (numOfBlasters>=2 && numOfMyBlasters==1) {
                world.pickHero(HeroName.BLASTER);
                numOfMyBlasters++;
            }
            else {
                world.pickHero(HeroName.GUARDIAN);
            }
        }
    }

    @Override
    public void moveTurn(World world) {
        System.out.println("move started");
        Hero[] heroes = world.getMyHeroes();
        Cell[] objZone=world.getMap().getObjectiveZone();
        if (world.getCurrentTurn()==4 && world.getMovePhaseNum()==0){
            heroesTactics=new HashMap<>();
            for (Hero h:heroes){
                heroesTactics.put(h.getId(),new GetToObjZoneTactic(objZone[random.nextInt(objZone.length)]));
            }
        }
        for (Hero hero : heroes)
        {
            if (world.getMovePhaseNum()<3 && !objectiveCells.exist(hero.getCurrentCell()) && !(heroesTactics.get(hero.getId()) instanceof GetToObjZoneTactic)) {
                heroesTactics.replace(hero.getId(), new GetToObjZoneTactic(world.getMap().getObjectiveZone()[random.nextInt(world.getMap().getObjectiveZone().length)]));
            }
            else {
                Hero[] enemies=world.getOppHeroes();
                List<Hero> byTeamVisibleEnemies=new ArrayList<>();
                for(Hero enemy:enemies){
                    if (enemy.getCurrentCell().getRow()!=-1)
                        byTeamVisibleEnemies.add(enemy);
                }
                Tuple<AVL_tree<Danger>, AVL_tree<Opportunity>> dangersAndOpportunes = getDangersAndOpportunitiesForHero(hero, world, byTeamVisibleEnemies.toArray(new Hero[]{}));
                Opportunity bestOpp = dangersAndOpportunes.getSecond().getMax();
                Danger mostSeriousDanger = dangersAndOpportunes.getFirst().getMax();
                String payAttentionTo = null;
                if (bestOpp != null) {
                    if (mostSeriousDanger != null) {
                        boolean weak=hero.getCurrentHP() < hero.getMaxHP() / 4;
                        if (weak) {
                            Hero helper = null;
                            for (Hero friend : heroes) {
                                int distanceToFriend = world.manhattanDistance(friend.getCurrentCell(), hero.getCurrentCell());
                                if (friend.getName() == HeroName.HEALER && friend.getAbility(AbilityName.HEALER_HEAL).isReady() && friend.getCurrentHP() > friend.getMaxHP() / 4 && distanceToFriend > friend.getAbility(AbilityName.HEALER_HEAL).getRange() && (distanceToFriend - (5 - world.getMovePhaseNum())) <= friend.getAbility(AbilityName.HEALER_HEAL).getRange()) {
                                    helper = friend;
                                    break;
                                }
                            }
                            if (helper!=null) {
                                payAttentionTo = "getHelp";
                                heroesTactics.replace(hero.getId(),new GetHelpTactic(helper.getAbility(AbilityName.HEALER_HEAL),helper.getCurrentCell()));
                            }
                            else
                                payAttentionTo="opportunity";
                        }
                        else
                            payAttentionTo = "opportunity";
                    } else {
                        payAttentionTo = "opportunity";
                    }
                } else if (mostSeriousDanger != null) {
                    payAttentionTo = "danger";
                }
                if (payAttentionTo == null) {
                    if (!objectiveCells.exist(hero.getCurrentCell())) {
                        if (!(heroesTactics.get(hero.getId()) instanceof GetToObjZoneTactic))
                            heroesTactics.replace(hero.getId(), new GetToObjZoneTactic(world.getMap().getObjectiveZone()[random.nextInt(world.getMap().getObjectiveZone().length)]));
                    }
                    else
                        heroesTactics.replace(hero.getId(), new HoldOnTactic(hero.getCurrentCell()));
                } else if (payAttentionTo.equals("opportunity")) {
                    if (bestOpp.type.getType() == AbilityType.DEFENSIVE)
                        heroesTactics.replace(hero.getId(), new DefenceTactic(bestOpp.type, bestOpp.in));
                    else if (bestOpp.type.getType() == AbilityType.OFFENSIVE)
                        heroesTactics.replace(hero.getId(), new OffendTactic(bestOpp.type, bestOpp.in, objectiveCells));
                } else if (payAttentionTo.equals("danger")){
                    if (hero.getDodgeAbilities()[0].isReady() && world.getMovePhaseNum() == 5 && hero.getCurrentHP() < hero.getMaxHP() / 4)
                        heroesTactics.replace(hero.getId(), new DodgeTactic(hero.getCurrentCell(), objectiveCells));
                    else {
                        int escapeAimX = ((3 * hero.getCurrentCell().getColumn()) - mostSeriousDanger.from.getColumn()) / 2;
                        int escapeAimY = ((3 * hero.getCurrentCell().getRow()) - mostSeriousDanger.from.getRow()) / 2;
                        heroesTactics.replace(hero.getId(), new EscapeTactic(new Cell(escapeAimY, escapeAimX)));
                    }
                }
            }
            Tactic tactic=heroesTactics.get(hero.getId());
            tactic.applyMove(hero,world);
        }
    }

    @Override
    public void actionTurn(World world) {
        System.out.println("action started");
        for (Hero hero:world.getMyHeroes()) {
            Tactic tactic = heroesTactics.get(hero.getId());
            tactic.applyAction(hero, world);
        }
    }

    private Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>> getDangersAndOpportunitiesForHero(Hero hero,World world,Hero[] byTeamVisibleEnemies){
        AVL_tree<Danger> dangers=new AVL_tree<>();
        AVL_tree<Opportunity> opportunities=new AVL_tree<>();
        for (Hero enemy:byTeamVisibleEnemies){
            if(enemy.getCurrentHP()==0)
                continue;
            boolean heroSeeEnemy=world.isInVision(hero.getCurrentCell(),enemy.getCurrentCell());
            int distanceBtwMeAndEnemy=world.manhattanDistance(hero.getCurrentCell(),enemy.getCurrentCell());
            boolean canOffend=false;
            boolean canDodge=true;
            for (CastAbility dAbility:world.getOppCastAbilities()){
                if (dAbility.getAbilityName()==enemy.getDodgeAbilities()[0].getName()) {
                    canDodge = false;
                    break;
                }
            }
            AbilityName pastCastAbiName=null;
            for(CastAbility pastAbility:world.getOppCastAbilities()){
                if (pastAbility.getCasterId()==enemy.getId()) {
                    pastCastAbiName=pastAbility.getAbilityName();
                    break;
                }
            }
            for (Ability ability:enemy.getOffensiveAbilities()){
                if ((heroSeeEnemy||ability.isLobbing()) && (pastCastAbiName==null||(pastCastAbiName!=ability.getName()))&& ability.getRange()>=distanceBtwMeAndEnemy/*-(5-world.getMovePhaseNum())*/) {
                    dangers.add(new Danger(enemy.getCurrentCell(), ability, hero, world.getMovePhaseNum()));
                    canOffend = true;
                }
            }
            boolean opportunityIsRisky=canDodge||canOffend;
            for (Ability ability:hero.getOffensiveAbilities()){
                if ( (heroSeeEnemy||ability.isLobbing())&& ability.isReady() && (ability.getRange()>=distanceBtwMeAndEnemy-(5-world.getMovePhaseNum())))
                    opportunities.add(new Opportunity(enemy.getCurrentCell(),ability,hero,enemy,world.getMovePhaseNum(),opportunityIsRisky));
            }
        }
        if (hero.getName()==HeroName.HEALER && hero.getDefensiveAbilities()[0].isReady()){
            for (Hero friend:world.getMyHeroes()){
                if (friend.getCurrentHP()==0)
                    continue;
                if (friend.getCurrentHP()<friend.getMaxHP()/5 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())-(5-world.getMovePhaseNum()))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend,world.getMovePhaseNum()));
            }
        }
        //defence guardian moonde
        return new Tuple<>(dangers,opportunities);
    }
}
