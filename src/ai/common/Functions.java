package ai.common;

import ai.Danger;
import ai.Opportunity;
import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
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
                if ( ability.isReady() && (ability.getRange()+ability.getAreaOfEffect()>=distanceBtwMeAndEnemy-(5-world.getMovePhaseNum()+1))) {
                    if(heroSeeEnemy||ability.isLobbing())
                        opportunities.add(new Opportunity(enemy.getCurrentCell(), ability, hero, enemy, world.getCurrentPhase(), opportunityIsRisky));
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
                        opportunities.add(new Opportunity(enemy.getCurrentCell(), ability, hero, enemy, world.getCurrentPhase(), opportunityIsRisky,true));
                    }
                }
            }
        }
        if (hero.getName()==HeroName.HEALER && hero.getDefensiveAbilities()[0].isReady()){
            for (Hero friend:world.getMyHeroes()){
                if (friend.getCurrentHP()==0)
                    continue;
                if (friend.getCurrentHP()<friend.getMaxHP()/2 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())-(5-world.getMovePhaseNum()+1))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend,world.getCurrentPhase()));
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
                opportunities.add(new Opportunity(mostDeserved.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,mostDeserved,world.getCurrentPhase()));
        }
        return new Tuple<>(dangers,opportunities);
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
            if (hero.getId()==me.getId())
                continue;
            for (int dRow=-neighboring;dRow<=neighboring;dRow++){
                for (int dCol=-neighboring;dCol<=neighboring;dCol++){
                    if (Math.abs(dRow)+Math.abs(dCol)>neighboring)
                        continue;
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
    public static Cell manhatanicNearestCellFarOfFriendsByAmount(World world,Hero me,Cell[] in,Cell to,int howFar){
        Cell best=null;
        int bestDist=Integer.MAX_VALUE;
        int candidDist;
        for (Cell candid:in){
            if (world.manhattanDistance(candid,to)>howFar)
                continue;
            candidDist=world.manhattanDistance(candid,to);
            if ((best==null || candidDist<bestDist)&& (world.getMyHero(candid)==null || world.getMyHero(candid).getId()==me.getId())){
                best=candid;
                bestDist=candidDist;
            }
        }
        return best;
    }
    public static List<Opportunity> clusterifyOpps(World world,List<Opportunity> opportunities){
        //I know that the list's length would not be more than 4
        if (opportunities.size()<=1)
            return opportunities;
        if (opportunities.size()==2){
            int dist=world.manhattanDistance(opportunities.get(0).in,opportunities.get(1).in);
            if (dist>4)
                return opportunities;
            ArrayList<Opportunity> res=new ArrayList<>();
            int meanHealth=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(1).aimHero.getCurrentHP())/2;
            if (dist<=2){
                res.add(new Opportunity(opportunities.get(0).in,opportunities.get(0).type,opportunities.get(0).forr,world.getCurrentPhase(),2,meanHealth));
                return res;
            }
            Cell mixedCell=world.getMap().getCell((opportunities.get(0).in.getRow()+opportunities.get(1).in.getRow())/2,(opportunities.get(0).in.getColumn()+opportunities.get(1).in.getColumn())/2);
            Opportunity mixed=new Opportunity(mixedCell,opportunities.get(0).type,opportunities.get(0).forr,world.getCurrentPhase(),2,meanHealth);
            res.add(mixed);
            return res;
        }
        else if (opportunities.size()==3){
            int dist01=world.manhattanDistance(opportunities.get(0).in,opportunities.get(1).in);
            int dist12=world.manhattanDistance(opportunities.get(1).in,opportunities.get(2).in);
            int dist20=world.manhattanDistance(opportunities.get(2).in,opportunities.get(0).in);
            ArrayList<Opportunity> res=new ArrayList<>();
            int range=opportunities.get(0).type.getAreaOfEffect();
            int whoBecomesAlone;
            if (dist01>2*range){
                if (dist12>2*range){
                    if (dist20>2*range){
                        return opportunities;
                    }
                    else {
                        whoBecomesAlone=1;
                    }
                }
                else {
                    if (dist20>2*range){
                        whoBecomesAlone=0;
                    }
                    else {
                        int meanHealth12=(opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                        int meanHealth20=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                        if (meanHealth12<=meanHealth20){
                            whoBecomesAlone=0;
                        }
                        else {
                            whoBecomesAlone=1;
                        }
                    }
                }
            }
            else {
                if (dist12>2*range){
                    if (dist20>2*range){
                        whoBecomesAlone=2;
                    }
                    else {
                        int meanHealth01=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(1).aimHero.getCurrentHP())/2;
                        int meanHealth20=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                        if (meanHealth01<=meanHealth20){
                            whoBecomesAlone=2;
                        }
                        else {
                            whoBecomesAlone=1;
                        }
                    }
                }
                else {
                    if (dist20>2*range){
                        int meanHealth01=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(1).aimHero.getCurrentHP())/2;
                        int meanHealth12=(opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                        if (meanHealth01<=meanHealth12){
                            whoBecomesAlone=2;
                        }
                        else {
                            whoBecomesAlone=0;
                        }
                    }
                    else{
                        double exactMeanRow=(opportunities.get(0).in.getRow()+opportunities.get(1).in.getRow()+opportunities.get(2).in.getRow())/3.0;
                        double exactMeanColumn=(opportunities.get(0).in.getColumn()+opportunities.get(1).in.getColumn()+opportunities.get(2).in.getColumn())/3.0;
                        System.out.println(exactMeanRow+","+exactMeanColumn);
                        int meanRow;
                        int meanColumn;
                        if (exactMeanRow-Math.floor(exactMeanRow)==0.5&&exactMeanColumn-Math.floor(exactMeanColumn)==0.5){
                            meanRow=(int)exactMeanRow;
                            meanColumn=(int)exactMeanColumn;
                        }
                        else if (exactMeanRow-Math.floor(exactMeanRow)<=0.5) {
                            if (exactMeanColumn - Math.floor(exactMeanColumn) <= 0.5) {
                                meanRow = (int) exactMeanRow - 1;
                                meanColumn = (int) exactMeanColumn;
                            } else {
                                meanRow = (int) exactMeanRow;
                                meanColumn = (int) exactMeanColumn + 1;
                            }
                        }else {
                            if (exactMeanColumn - Math.floor(exactMeanColumn) <= 0.5){
                                meanRow = (int) exactMeanRow ;
                                meanColumn = (int) exactMeanColumn-1;
                            }
                            else{
                                meanRow = (int) exactMeanRow + 1;
                                meanColumn = (int) exactMeanColumn;
                            }
                        }
                        Cell mean012=world.getMap().getCell(meanRow,meanColumn);
                        boolean canMix=true;
                        for(Opportunity opp:opportunities){
                            if (world.manhattanDistance(opp.in,mean012)>range)
                                canMix=false;
                        }
                        if (canMix){
                            System.out.println("bettrekoon: "+mean012.getRow()+","+mean012.getColumn());
                            int meanHealth012=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/3;
                            res.add(new Opportunity(mean012,opportunities.get(0).type,opportunities.get(0).forr,world.getCurrentPhase(),3,meanHealth012));
                            return res;
                        }
                        else {
                            int meanHealth01=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(1).aimHero.getCurrentHP())/2;
                            int meanHealth12=(opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                            int meanHealth20=(opportunities.get(2).aimHero.getCurrentHP()+opportunities.get(0).aimHero.getCurrentHP())/2;
                            if (meanHealth01<Math.min(meanHealth12,meanHealth20)){
                                whoBecomesAlone=2;
                            }
                            else if (meanHealth12<Math.min(meanHealth01,meanHealth20)){
                                whoBecomesAlone=0;
                            }
                            else {
                                whoBecomesAlone=1;
                            }
                        }
                    }
                }
            }
            if (whoBecomesAlone==0){
                res.add(opportunities.get(0));
                int meanHealth=(opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                Cell mixedCell=world.getMap().getCell((opportunities.get(1).in.getRow()+opportunities.get(2).in.getRow())/2,(opportunities.get(1).in.getColumn()+opportunities.get(2).in.getColumn())/2);
                System.out.println("0 tanhast .hadaf:"+mixedCell.getRow()+","+mixedCell.getColumn());
                Opportunity mixed=new Opportunity(mixedCell,opportunities.get(1).type,opportunities.get(1).forr,world.getCurrentPhase(),2,meanHealth);
                res.add(mixed);
                return res;
            }
            else if (whoBecomesAlone==1){
                res.add(opportunities.get(1));
                int meanHealth=(opportunities.get(0).aimHero.getCurrentHP()+opportunities.get(2).aimHero.getCurrentHP())/2;
                Cell mixedCell=world.getMap().getCell((opportunities.get(0).in.getRow()+opportunities.get(2).in.getRow())/2,(opportunities.get(0).in.getColumn()+opportunities.get(2).in.getColumn())/2);
                System.out.println("1 tanhast .hadaf:"+mixedCell.getRow()+","+mixedCell.getColumn());
                Opportunity mixed=new Opportunity(mixedCell,opportunities.get(0).type,opportunities.get(0).forr,world.getCurrentPhase(),2,meanHealth);
                res.add(mixed);
                return res;
            }
            else if (whoBecomesAlone==2){
                res.add(opportunities.get(2));
                int meanHealth=(opportunities.get(1).aimHero.getCurrentHP()+opportunities.get(0).aimHero.getCurrentHP())/2;
                Cell mixedCell=world.getMap().getCell((opportunities.get(1).in.getRow()+opportunities.get(0).in.getRow())/2,(opportunities.get(1).in.getColumn()+opportunities.get(0).in.getColumn())/2);
                System.out.println("2 tanhast .hadaf:"+mixedCell.getRow()+","+mixedCell.getColumn());
                Opportunity mixed=new Opportunity(mixedCell,opportunities.get(1).type,opportunities.get(1).forr,world.getCurrentPhase(),2,meanHealth);
                res.add(mixed);
                return res;
            }
            else {
                System.out.println("nashod ke");
                return opportunities;
            }
        }
        else return opportunities;
        //TODo age chahar ta bood forsata
    }
}
