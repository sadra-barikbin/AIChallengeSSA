package ai;

import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ai.common.Functions.*;

public class StructuredComplexAI implements AbstractAI {
    private java.util.Map<Integer,Tactic> heroesTactics;
    @Override
    public void preProcess(World world) {
        System.out.println("preprocess in structuredComplexAI started");
    }

    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        if (world.getCurrentTurn()==0)
            world.pickHero(HeroName.BLASTER);
        else if (world.getCurrentTurn()==1) {
//            if (world.getOppHeroes()[0].getName() != HeroName.BLASTER)
                //world.pickHero(HeroName.HEALER);
//            else
                world.pickHero(HeroName.BLASTER);
        }
        else if(world.getCurrentTurn()==2){
//            if (world.getOppHeroes()[1].getName()!=HeroName.BLASTER)
//                world.pickHero(HeroName.SENTRY);
//            else
                world.pickHero(HeroName.BLASTER);
            //world.pickHero(HeroName.HEALER);
        }
        else if (world.getCurrentTurn()==3){
//            if (world.getOppHeroes()[2].getName()!=HeroName.BLASTER)
//                world.pickHero(HeroName.GUARDIAN);
//            else
                world.pickHero(HeroName.BLASTER);
//            world.pickHero(HeroName.HEALER);
        }

    }

    @Override
    public void moveTurn(World world) {
        System.out.println("move started");
        Hero[] liveHeroes=getMyLiveHeroes(world);
        Cell[] objZone=world.getMap().getObjectiveZone();
        if (world.getCurrentTurn()==4 && world.getMovePhaseNum()==0){
            heroesTactics=new HashMap<>();
            for (Hero h:liveHeroes){
                heroesTactics.put(h.getId(),new GetToObjZoneTactic(manhatanicNearestCellEmptyOfFriend(world,h,objZone,h.getCurrentCell())));
            }
        }
        Hero[] enemies=world.getOppHeroes();
        List<Hero> byTeamVisibleEnemies=new ArrayList<>();
        for(Hero enemy:enemies){
            if (enemy.getCurrentCell().getRow()!=-1)
                byTeamVisibleEnemies.add(enemy);
        }
        java.util.Map<Integer, Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>>> heroesDansAndOpps=new HashMap<>();
        for (Hero hero:liveHeroes)//I assumed if i have Guardian,it has been picked lastly.
            heroesDansAndOpps.put(hero.getId(),getDangersAndOpportunitiesForHero(hero,world,byTeamVisibleEnemies.toArray(new Hero[]{}),heroesDansAndOpps));
        for (Hero hero:liveHeroes){
            heroesTactics.replace(hero.getId(),resolveMovePhaseTactic(world,hero,heroesTactics,heroesDansAndOpps,liveHeroes,objZone));
            Tactic tactic=heroesTactics.get(hero.getId());
            tactic.applyMove(hero,world);
        }
    }

    @Override
    public void actionTurn(World world) {
        System.out.println("action started");
        Map<Integer,List<Danger>> heroesDangers=new HashMap<>();
        for (Hero hero:getMyLiveHeroes(world))
            heroesDangers.put(hero.getId(),getDangersInActionPhase(world,hero));
        for (Hero hero:getMyLiveHeroes(world)) {
            heroesTactics.replace(hero.getId(),resolveActionPhaseTactic(world,hero,heroesDangers));
            Tactic tactic = heroesTactics.get(hero.getId());
            tactic.applyAction(hero, world);
        }
    }
    private Tactic resolveActionPhaseTactic(World world,Hero hero,java.util.Map<Integer,List<Danger>> heroesDangers){
        if (world.getAP()>=hero.getDodgeAbilities()[0].getAPCost()&& hero.getDodgeAbilities()[0].isReady() && (heroesDangers.get(hero.getId()).size()>4 || (hero.getCurrentHP()<hero.getMaxHP()/4 && heroesDangers.get(hero.getId()).size()>0))) {
            Cell whereToDodge=getGoodDodgeTarget(hero, world);
            if (whereToDodge!=null) {
                world.castAbility(hero, hero.getDodgeAbilities()[0], whereToDodge);
                return new DodgeTactic(whereToDodge);
            }
        }
        AVL_tree<Opportunity> opportunities=getOpportunitiesInActionPhase(world,hero,heroesDangers);
        Opportunity bestOpp=opportunities.getMax();
        if (bestOpp!=null)opportunities.delete(bestOpp);
        if (bestOpp!=null && bestOpp.type.getAPCost()>world.getAP()){
            while (bestOpp!= null && bestOpp.type.getAPCost()>world.getAP()){
                bestOpp=opportunities.getMax();
                if (bestOpp!=null)opportunities.delete(bestOpp);
            }
        }
        if (bestOpp==null)
            return new HoldOnTactic(hero.getCurrentCell());
        if (bestOpp.type.getType()== AbilityType.OFFENSIVE)
            return new OffendTactic(bestOpp.type,bestOpp.in);
        else
            return new DefenceTactic(bestOpp.type,bestOpp.in);

    }
    private AVL_tree<Opportunity> getOpportunitiesInActionPhase(World world,Hero hero,java.util.Map<Integer,List<Danger>> heroesDangers){
        AVL_tree<Opportunity> opportunities=new AVL_tree<>();
        java.util.Map<Ability,List<Opportunity>> offensiveOppsApartByAbilities=new HashMap<>();
        List<Opportunity> outOfAbilityRangeOpps=new ArrayList<>();
        for (Ability ability:hero.getOffensiveAbilities())
            offensiveOppsApartByAbilities.put(ability,new ArrayList<>());
        for (Hero enemy:getVisibleEnemies(world)){
            boolean heroSeeEnemy=world.isInVision(hero.getCurrentCell(),enemy.getCurrentCell());
            int distanceBtwMeAndEnemy=world.manhattanDistance(hero.getCurrentCell(),enemy.getCurrentCell());
            for (Ability ability:hero.getOffensiveAbilities()){
                if ( ability.isReady() && (ability.getRange()+ability.getAreaOfEffect()>=distanceBtwMeAndEnemy)) {
                    if(heroSeeEnemy||ability.isLobbing()) {
                        Cell aimCell=enemy.getCurrentCell();
                        if (ability.getRange()<distanceBtwMeAndEnemy){
                            System.out.println("azizam");
                            System.out.println("ability AF:"+ability.getAreaOfEffect());
                            System.out.println("myHero:"+hero.getCurrentCell().getRow()+","+hero.getCurrentCell().getColumn());
                            System.out.println("aimCell avvaliye:"+aimCell.getRow()+","+aimCell.getColumn());
                            if (hero.getCurrentCell().getColumn()==aimCell.getColumn()){
                                if (aimCell.getRow()>hero.getCurrentCell().getRow())
                                    aimCell=world.getMap().getCell(aimCell.getRow()-ability.getAreaOfEffect(),aimCell.getColumn());
                                else
                                    aimCell=world.getMap().getCell(aimCell.getRow()+ability.getAreaOfEffect(),aimCell.getColumn());
                            }
                            else {
                                double slope = -((double)hero.getCurrentCell().getRow() - aimCell.getRow()) / ((double) hero.getCurrentCell().getColumn() - aimCell.getColumn());
                                double deltaCol;
                                double deltaRow;
                                if (hero.getCurrentCell().getColumn() >aimCell.getColumn()) {
                                    if (hero.getCurrentCell().getRow()>aimCell.getRow()){
                                        deltaCol = ability.getAreaOfEffect() / (1.0 - slope);
                                    }
                                    else {
                                        deltaCol=ability.getAreaOfEffect()/(slope+1.0);
                                    }
                                }
                                else {
                                    if (hero.getCurrentCell().getRow()>aimCell.getRow()){
                                        deltaCol = -ability.getAreaOfEffect() / (1.0 - slope);
                                    }
                                    else {
                                        deltaCol=ability.getAreaOfEffect()/(slope-1.0);
                                    }
                                }
                                deltaRow=-slope*deltaCol;
                                aimCell=world.getMap().getCell(aimCell.getRow()+(int)Math.round(deltaRow),aimCell.getColumn()+(int)Math.round(deltaCol));
                            }
                            System.out.println("aimCell sanaviie:"+aimCell.getRow()+","+aimCell.getColumn());
                        }
                        Opportunity o=new Opportunity(aimCell, ability, hero, enemy, world.getCurrentPhase());
                        if(ability.getRange()<distanceBtwMeAndEnemy)
                            outOfAbilityRangeOpps.add(o);
                        else
                            offensiveOppsApartByAbilities.get(ability).add(o);
                    }
                }
            }
        }
        for (Ability ability:offensiveOppsApartByAbilities.keySet()){
            opportunities.addAll(clusterifyOpps(world,offensiveOppsApartByAbilities.get(ability)));

        }
        opportunities.addAll(outOfAbilityRangeOpps);
        if (hero.getName()==HeroName.HEALER && hero.getDefensiveAbilities()[0].isReady()){
            for (Hero friend:getMyLiveHeroes(world)){
                if (friend.getCurrentHP()<= (friend.getMaxHP()-hero.getDefensiveAbilities()[0].getPower()) && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell()))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend,world.getCurrentPhase()));
            }
        }
        else if (hero.getName()==HeroName.GUARDIAN && hero.getDefensiveAbilities()[0].isReady()){
            Hero mostDeserved=null;
            int mostDeservedDangersCount=0;
            for (Hero friend:getMyLiveHeroes(world)){
                if (friend.getName()==HeroName.GUARDIAN)//this says Guardian does not guard any Guardian
                    continue;
                int dangersCount=heroesDangers.get(friend.getId()).size();
                if (dangersCount>4 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())){
                    if (mostDeserved==null || dangersCount>mostDeservedDangersCount || friend.getCurrentHP()<mostDeserved.getCurrentHP()) {
                        mostDeserved = friend;
                        mostDeservedDangersCount=dangersCount;
                    }
                }
            }
            if (mostDeserved!=null)
                opportunities.add(new Opportunity(mostDeserved.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,mostDeserved,world.getCurrentPhase()));
        }
        return opportunities;
    }
    private List<Danger> getDangersInActionPhase(World world,Hero hero){
        Hero[] byTeamVisibleEnemies=getVisibleEnemies(world);
        List<Danger> toReturn=new ArrayList<>();
        for (Hero enemy:byTeamVisibleEnemies) {
            if (enemy.getCurrentHP() == 0)
                continue;
            boolean heroSeeEnemy = world.isInVision(hero.getCurrentCell(), enemy.getCurrentCell());
            int distanceBtwMeAndEnemy = world.manhattanDistance(hero.getCurrentCell(), enemy.getCurrentCell());
            AbilityName pastCastAbiName = null;
            for (CastAbility pastAbility : world.getOppCastAbilities()) {
                if (pastAbility.getCasterId() == enemy.getId()) {
                    pastCastAbiName = pastAbility.getAbilityName();
                    break;
                }
            }
            for (Ability ability : enemy.getOffensiveAbilities()) {
                if ((heroSeeEnemy || ability.isLobbing()) && (pastCastAbiName == null || (pastCastAbiName != ability.getName())) && ability.getRange() >= distanceBtwMeAndEnemy) {
                    toReturn.add(new Danger(enemy.getCurrentCell(), ability, hero));
                }
            }
        }
        return toReturn;
    }
    Tactic resolveMovePhaseTactic(World world, Hero hero,java.util.Map<Integer,Tactic> lastHeroesTactics, java.util.Map<Integer,Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>>> heroesDansAndOpps,Hero[] liveHeroes,Cell[] objZone){
        if (world.getMovePhaseNum()<3 && !hero.getCurrentCell().isInObjectiveZone() && !(lastHeroesTactics.get(hero.getId()) instanceof GetToObjZoneTactic))
            return new GetToObjZoneTactic(manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell()));
        boolean weak=hero.getCurrentHP() < hero.getMaxHP() / 5;
        Hero reachableHealer = null;
        boolean isAnyHealthyHealer=false;
        for (Hero friend : liveHeroes) {
            int distanceToFriend = world.manhattanDistance(friend.getCurrentCell(), hero.getCurrentCell());
            if (friend.getName()==HeroName.HEALER && friend.getCurrentHP()>friend.getMaxHP()/4) {
                isAnyHealthyHealer = true;
                if ( friend.getAbility(AbilityName.HEALER_HEAL).isReady() && distanceToFriend > friend.getAbility(AbilityName.HEALER_HEAL).getRange() && (distanceToFriend - (5 - world.getMovePhaseNum() + 1)) <= friend.getAbility(AbilityName.HEALER_HEAL).getRange()) {
                    reachableHealer = friend;
                    break;
                }
            }
        }
        if (weak && reachableHealer!=null)
            return new GetHelpTactic(reachableHealer.getAbility(AbilityName.HEALER_HEAL),reachableHealer.getCurrentCell());
        Opportunity bestOpp=heroesDansAndOpps.get(hero.getId()).getSecond().getMax();
        Danger worstDanger=heroesDansAndOpps.get(hero.getId()).getFirst().getMax();
        if (bestOpp==null && worstDanger==null)
            return new GetToObjZoneTactic(manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell()));
        if(bestOpp!=null){
            if (bestOpp.type.getType()==AbilityType.OFFENSIVE)
                return new OffendTactic(bestOpp.type, bestOpp.in);
            else if (bestOpp.type.getType()==AbilityType.DEFENSIVE)
                return new DefenceTactic(bestOpp.type, bestOpp.in);
            else
                throw new RuntimeException("just offensive or defensive here");
        }
//        if (bestOpp==null && worstDanger!=null){
        return new GetToObjZoneTactic(manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell()));
//        }
    }
}
