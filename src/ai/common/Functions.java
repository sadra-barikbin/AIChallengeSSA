package ai.common;

import ai.Danger;
import ai.Opportunity;
import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.List;

public class Functions {
    public static Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>> getDangersAndOpportunitiesForHero(Hero hero, World world, Hero[] byTeamVisibleEnemies){
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
    public static Cell manhatanicNearestCellEmptyOfFriend(World world,Hero me,Cell[] in,Cell to){
        Cell best=null;
        int bestDist=Integer.MAX_VALUE;
        int candidDist;
        for (Cell candid:in){
            candidDist=world.manhattanDistance(candid,to);
            if ((best==null || candidDist<bestDist)&& (world.getMyHero(candid)==null || world.getMyHero(candid).getId()==me.getId())){
                best=candid;
                bestDist=candidDist;
            }
        }
        return best;
    }
    /*public static Tactic resolveTactic(World world, Hero hero,java.util.Map<Integer,Tactic> lastHeroesTactics, java.util.Map<Integer,Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>>> heroesDansAndOpps,List<Hero> liveHeroes, List<Hero> byTeamVisibleEnemies,Cell[] objZone){
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
        if(bestOpp!=null && worstDanger==null){
            if (bestOpp.type.getType()==AbilityType.OFFENSIVE)
                return new OffendTactic(bestOpp.type, bestOpp.in);
            else if (bestOpp.type.getType()==AbilityType.DEFENSIVE)
                return new DefenceTactic(bestOpp.type, bestOpp.in);
            else
                throw new RuntimeException("just offensive or defensive here");
        }
        return null;
    }*/
}
