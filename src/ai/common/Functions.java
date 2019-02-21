package ai.common;

import ai.Danger;
import ai.Opportunity;
import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Functions {
    public static Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>> getDangersAndOpportunitiesForHero(Hero hero, World world, Hero[] byTeamVisibleEnemies,java.util.Map<Integer, Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>>> heroesDansAndOppsForGuardianFortify){
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
                    dangers.add(new Danger(enemy.getCurrentCell(), ability, hero));
                    canOffend = true;
                }
            }
            boolean opportunityIsRisky=canDodge||canOffend;
            for (Ability ability:hero.getOffensiveAbilities()){
                if ( ability.isReady() && (ability.getRange()>=distanceBtwMeAndEnemy-(5-world.getMovePhaseNum()+1))) {
                    if(heroSeeEnemy||ability.isLobbing())
                        opportunities.add(new Opportunity(enemy.getCurrentCell(), ability, hero, enemy, world.getMovePhaseNum(), opportunityIsRisky));
                    else {
                        /*Cell firstCellInPathWithEnemyVisible;
                        Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),enemy.getCurrentCell(),getMyLiveHeroesPlacesButMe(world,hero));
                        Cell temp=hero.getCurrentCell();
                        int pathIndex=0;
                        while (!world.isInVision(temp,enemy.getCurrentCell())) {
                            temp = applyDirectionToCell(world, temp, path[pathIndex]);
                            pathIndex++;
                        }
                        firstCellInPathWithEnemyVisible=temp;*/
                        opportunities.add(new Opportunity(enemy.getCurrentCell(), ability, hero, enemy, world.getMovePhaseNum(), opportunityIsRisky,true));
                    }
                }
            }
        }
        if (hero.getName()==HeroName.HEALER && hero.getDefensiveAbilities()[0].isReady()){
            for (Hero friend:world.getMyHeroes()){
                if (friend.getCurrentHP()==0)
                    continue;
                if (friend.getCurrentHP()<friend.getMaxHP()/2 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())-(5-world.getMovePhaseNum()+1))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend,world.getMovePhaseNum()));
            }
        }
        else if (heroesDansAndOppsForGuardianFortify!=null && hero.getName()==HeroName.GUARDIAN && hero.getDefensiveAbilities()[0].isReady()){
            Hero mostDeserved=null;
            int mostDeservedDangersCount=0;
            for (Hero friend:world.getMyHeroes()){
                if (friend.getCurrentHP()==0 || friend.getName()==HeroName.GUARDIAN)//this says Guardian does not guard any Guardian
                    continue;
                int dangersCount=heroesDansAndOppsForGuardianFortify.get(friend.getId()).getFirst().getCount();
                if (dangersCount>4 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())-(5-world.getMovePhaseNum()+1)){
                    if (mostDeserved==null || dangersCount>mostDeservedDangersCount || friend.getCurrentHP()<mostDeserved.getCurrentHP()) {
                        mostDeserved = friend;
                        mostDeservedDangersCount=dangersCount;
                    }
                }
            }
            if (mostDeserved!=null)
                opportunities.add(new Opportunity(mostDeserved.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,mostDeserved,world.getMovePhaseNum()));
        }
        return new Tuple<>(dangers,opportunities);
    }

    public static Tactic resolveMovePhaseTactic(World world, Hero hero,java.util.Map<Integer,Tactic> lastHeroesTactics, java.util.Map<Integer,Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>>> heroesDansAndOpps,Hero[] liveHeroes,Cell[] objZone){
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
    public static Tactic resolveActionPhaseTactic(World world,Hero hero,java.util.Map<Integer,List<Danger>> heroesDangers){
        if (hero.getDodgeAbilities()[0].isReady() && (heroesDangers.get(hero.getId()).size()>4 || (hero.getCurrentHP()<hero.getMaxHP()/4))) {
                Cell whereToDodge=getGoodDodgeTarget(hero, world);
                if (whereToDodge!=null) {
                    world.castAbility(hero, hero.getDodgeAbilities()[0], whereToDodge);
                    return new DodgeTactic(whereToDodge);
                }
        }
        AVL_tree<Opportunity> opportunities=getOpportunitiesInActionPhase(world,hero,heroesDangers);
        Opportunity bestOpp=opportunities.getMax();
        if (bestOpp==null)
            return new HoldOnTactic(hero.getCurrentCell());
        if (bestOpp.type.getType()==AbilityType.OFFENSIVE)
            return new OffendTactic(bestOpp.type,bestOpp.in);
        else
            return new DefenceTactic(bestOpp.type,bestOpp.in);

    }
    public static AVL_tree<Opportunity> getOpportunitiesInActionPhase(World world,Hero hero,java.util.Map<Integer,List<Danger>> heroesDangers){
        AVL_tree<Opportunity> opportunities=new AVL_tree<>();
        for (Hero enemy:getVisibleEnemies(world)){
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
                if ((heroSeeEnemy||ability.isLobbing()) && (pastCastAbiName==null||(pastCastAbiName!=ability.getName()))&& ability.getRange()>=distanceBtwMeAndEnemy) {
                    canOffend = true;
                }
            }
            boolean opportunityIsRisky=canDodge||canOffend;
            for (Ability ability:hero.getOffensiveAbilities()){
                if ( ability.isReady() && (ability.getRange()>=distanceBtwMeAndEnemy)) {
                    if(heroSeeEnemy||ability.isLobbing())
                        opportunities.add(new Opportunity(enemy.getCurrentCell(), ability, hero, enemy, opportunityIsRisky));
                }
            }
        }
        if (hero.getName()==HeroName.HEALER && hero.getDefensiveAbilities()[0].isReady()){
            for (Hero friend:getMyLiveHeroes(world)){
                if (friend.getCurrentHP()<= (friend.getMaxHP()-hero.getDefensiveAbilities()[0].getPower()) && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell()))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend));
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
                opportunities.add(new Opportunity(mostDeserved.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,mostDeserved));
        }
        return opportunities;
    }
    public static List<Danger> getDangersInActionPhase(World world,Hero hero){
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
    public static Cell getGoodDodgeTarget(Hero hero,World world){
        int maxJump=hero.getDodgeAbilities()[0].getRange();
        Cell bestOption=null;
        Cell bestBestOption=null;
        Cell currentCell=hero.getCurrentCell();
        for (int dx=-maxJump;dx<=maxJump;dx++){
            for (int dy=-maxJump;dy<=maxJump;dy++){
                if (Math.abs(dx)+Math.abs(dy)>maxJump ||(dx==0 && dy==0))
                    continue;
                int newX=currentCell.getColumn()+dx;
                int newY=currentCell.getRow()+dy;
                if (world.getMap().isInMap(newY,newX) && !world.getMap().getCell(newY,newX).isWall() && world.getMyHero(newY,newX)==null)
                {
                    Cell temp=world.getMap().getCell(newY,newX);
                    if (temp.isInObjectiveZone()) {
                        if (bestBestOption==null ||(world.manhattanDistance(hero.getCurrentCell(),bestBestOption)<world.manhattanDistance(hero.getCurrentCell(),temp)))
                            bestBestOption=temp;
                    }
                    else if (bestOption==null ||(world.manhattanDistance(hero.getCurrentCell(),bestOption)<world.manhattanDistance(hero.getCurrentCell(),temp)) )
                        bestOption=temp;
                }
            }
        }
        if (bestBestOption!=null)
            return bestBestOption;
        else if (bestOption!=null)
            return bestOption;
        else return null;
    }
    public static Cell applyDirectionToCell(World world,Cell c,Direction d){
        switch (d){
            case UP:{
                return world.getMap().getCell(c.getRow()-1,c.getColumn());
            }
            case DOWN:{
                return world.getMap().getCell(c.getRow()+1,c.getColumn());
            }
            case LEFT:{
                return world.getMap().getCell(c.getRow(),c.getColumn()-1);
            }
            case RIGHT:{
                return world.getMap().getCell(c.getRow(),c.getColumn()+1);
            }
            default:{
                return c;
            }
        }
    }
    public static Hero[] getMyLiveHeroes(World world){
        Hero[] allHeroes = world.getMyHeroes();
        List<Hero> liveHeroes=new ArrayList<>();
        for (Hero hero:allHeroes)
            if (hero.getCurrentHP()!=0)
                liveHeroes.add(hero);
        return liveHeroes.toArray(new Hero[]{});
    }
    public static Cell[] getMyLiveHeroesPlacesButMe(World world,Hero me){
        Hero[] allHeroes = world.getMyHeroes();
        List<Cell> liveHeroes=new ArrayList<>();
        for (Hero hero:allHeroes)
            if (hero.getCurrentHP()!=0 && hero.getId()!=me.getId())
                liveHeroes.add(hero.getCurrentCell());
        return liveHeroes.toArray(new Cell[]{});
    }
    public static Cell[] getMyLiveHeroesPlacesAndTheirNeighboringButMe(World world,Hero me,int neighboring){
        AVL_tree<Cell> toRet=new AVL_tree<>();
        for (Hero hero:getMyLiveHeroes(world)){
            for (int dRow=-neighboring;dRow<=neighboring;dRow++){
                for (int dCol=-neighboring;dCol<=neighboring;dCol++){
                    Cell neighbor=world.getMap().getCell(hero.getCurrentCell().getRow()+dRow,hero.getCurrentCell().getColumn()+dCol);
                    if (neighbor!=null && !neighbor.equals(me.getCurrentCell()) && !neighbor.isWall())
                        toRet.addIfNotDuplicate(neighbor);
                }
            }
        }
        return toRet.in_order_traversal().toArray(new Cell[]{});
    }
    public static Hero[] getVisibleEnemies(World world){
        List<Hero> byTeamVisibleEnemies=new ArrayList<>();
        for(Hero enemy:world.getOppHeroes()){
            if (enemy.getCurrentCell().getRow()!=-1)
                byTeamVisibleEnemies.add(enemy);
        }
        return byTeamVisibleEnemies.toArray(new Hero[]{});
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
}
