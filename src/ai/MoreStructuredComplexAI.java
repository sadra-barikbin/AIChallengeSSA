package ai;

import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.Map;

import static ai.common.Functions.manhatanicNearestCellEmptyOfFriend;
import static ai.common.Functions.manhatanicNearestCellFarOfFriendsByAmount;

public class MoreStructuredComplexAI extends StructuredComplexAI{
    @Override
    public void preProcess(World world) {
        System.out.println("preprocess in MoreStructuredComplexAI started");
    }
    @Override
    Tactic resolveMovePhaseTactic(World world, Hero hero, Map<Integer, Tactic> lastHeroesTactics, Map<Integer, Tuple<AVL_tree<Danger>, AVL_tree<Opportunity>>> heroesDansAndOpps, Hero[] liveHeroes, Cell[] objZone) {
        if (world.getMovePhaseNum()<3 && !hero.getCurrentCell().isInObjectiveZone() && !(lastHeroesTactics.get(hero.getId()) instanceof GetToObjZoneTactic))
            return new GetToObjZoneTactic(manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell()));
        boolean weak=hero.getCurrentHP() < hero.getMaxHP() / 5;
        Hero reachableHealer = null;
        boolean isAnyHealthyHealer=false;
        for (Hero friend : liveHeroes) {
            int distanceToFriend = world.manhattanDistance(friend.getCurrentCell(), hero.getCurrentCell());
            if (friend.getName()== HeroName.HEALER && friend.getCurrentHP()>friend.getMaxHP()/4) {
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
        if (bestOpp==null && worstDanger==null) {
            Cell aim=manhatanicNearestCellFarOfFriendsByAmount(world, hero, objZone, hero.getCurrentCell(), 4);
            if (aim==null)
                aim=manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell());
            return new GetToObjZoneTactic(aim);
        }
        if(bestOpp!=null){
            if (bestOpp.type.getType()==AbilityType.OFFENSIVE)
                return new OffendTactic(bestOpp.type, bestOpp.in);
            else if (bestOpp.type.getType()==AbilityType.DEFENSIVE)
                return new DefenceTactic(bestOpp.type, bestOpp.in);
            else
                throw new RuntimeException("just offensive or defensive here");
        }
        Cell aim=manhatanicNearestCellFarOfFriendsByAmount(world,hero,objZone,hero.getCurrentCell(),4);
        if (aim==null)
            aim=manhatanicNearestCellEmptyOfFriend(world,hero,objZone,hero.getCurrentCell());
        return new GetToObjZoneTactic(aim);
    }
}