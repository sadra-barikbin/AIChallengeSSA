package ai;

import ai.tactics.GetToObjZoneTactic;
import ai.tactics.Tactic;
import client.model.Cell;
import client.model.Hero;
import client.model.HeroName;
import client.model.World;
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
        System.out.println("preprocess started");
    }

    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        if (world.getCurrentTurn()==0)
            world.pickHero(HeroName.BLASTER);
        else if (world.getCurrentTurn()==1) {
//            if (world.getOppHeroes()[0].getName() != HeroName.BLASTER)
//                world.pickHero(HeroName.HEALER);
//            else
                world.pickHero(HeroName.BLASTER);
        }
        else if(world.getCurrentTurn()==2){
//            if (world.getOppHeroes()[1].getName()!=HeroName.BLASTER)
//                world.pickHero(HeroName.SENTRY);
//            else
                world.pickHero(HeroName.BLASTER);
        }
        else if (world.getCurrentTurn()==3){
//            if (world.getOppHeroes()[2].getName()!=HeroName.BLASTER)
//                world.pickHero(HeroName.GUARDIAN);
//            else
                world.pickHero(HeroName.BLASTER);
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
}
