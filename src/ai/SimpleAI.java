package ai;

import ai.tactics.GetToObjZoneTactic;
import ai.tactics.OffendTactic;
import ai.tactics.Tactic;
import client.model.*;
import client.model.Map;

import java.util.*;

/**
made by Sadra
 **/
public class SimpleAI implements AbstractAI {
    private Random random = new Random();
    private java.util.Map<Integer,Cell> heroesPositionalAims;
    private Tactic tactic;
    @Override
    public void preProcess(World world) {
        System.out.println("preProcess in simpleAI started");
        tactic=new GetToObjZoneTactic(null);
    }

    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        world.pickHero(HeroName.BLASTER);
    }

    @Override
    public void moveTurn(World world) {
        System.out.println("move started");
        Hero[] heroes = world.getMyHeroes();
        Cell[] objZone=world.getMap().getObjectiveZone();
        if (world.getCurrentTurn()==4 && world.getMovePhaseNum()==0){
            heroesPositionalAims=new HashMap<>();
            for (Hero h:heroes){
                heroesPositionalAims.put(h.getId(),objZone[random.nextInt(objZone.length)]);
            }
        }
        int inObjZoneHeroesCnt=0;
        for (Hero hero : heroes)
        {
            if(!(tactic instanceof GetToObjZoneTactic)) {
                if (hero.getName() == HeroName.GUARDIAN && world.getMovePhaseNum() != 1)
                    continue;
                if (hero.getName() == HeroName.BLASTER && world.getMovePhaseNum() % 4 != 0)
                    continue;
                if (hero.getName() == HeroName.SENTRY && world.getMovePhaseNum() % 2 != 0)
                    continue;
                if (hero.getName() == HeroName.HEALER && world.getMovePhaseNum() % 4 != 0)
                    continue;
            }
            Direction[] goodPath=world.getPathMoveDirections(hero.getCurrentCell(),heroesPositionalAims.get(hero.getId()));
            if (goodPath.length==0)
            {
                if (tactic instanceof GetToObjZoneTactic)
                    inObjZoneHeroesCnt++;
                //farar az daste doshmanane nazdik
            }
            else{
                Direction goodDir=goodPath[0];
                world.moveHero(hero, goodDir);
            }
        }
        if (tactic instanceof GetToObjZoneTactic && inObjZoneHeroesCnt==heroes.length)
            tactic=new OffendTactic(null,null,null);
    }

    @Override
    public void actionTurn(World world) {
        System.out.println("action started");
        List<Hero> enemies=Arrays.asList(world.getOppHeroes());
        List<Hero> toBeHealed=new ArrayList<>();
        Hero[] myHeroes=world.getMyHeroes();
        for(Hero hero:myHeroes){
            if (hero.getCurrentHP()<hero.getMaxHP()/5)
                toBeHealed.add(hero);
        }
        for (Hero hero:myHeroes){
            if (hero.getName()==HeroName.HEALER){
                Hero mostInjured=mostInjuredAndNear(world,hero,4,toBeHealed);
                if (mostInjured!=null && hero.getAbility(AbilityName.HEALER_HEAL).isReady()){
                    world.castAbility(hero,AbilityName.HEALER_HEAL,mostInjured.getCurrentCell());
                }else {
                    Hero weakestEnemy = mostInjuredAndNear(world, hero,4, enemies);
                    if (weakestEnemy!=null && hero.getAbility(AbilityName.HEALER_ATTACK).isReady())
                        world.castAbility(hero,AbilityName.HEALER_ATTACK,weakestEnemy.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.BLASTER){
                Hero weakestEnemy=mostInjuredAndNear(world,hero,5,enemies);
                if (weakestEnemy!=null && hero.getAbility(AbilityName.BLASTER_BOMB).isReady())
                    world.castAbility(hero,AbilityName.BLASTER_BOMB,weakestEnemy.getCurrentCell());
                else{
                    weakestEnemy=mostInjuredAndVisiblyNear(world,hero,4,enemies);
                    if (weakestEnemy!=null && hero.getAbility(AbilityName.BLASTER_ATTACK).isReady())
                        world.castAbility(hero,AbilityName.BLASTER_ATTACK,hero.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.SENTRY){
                Hero weakestEnemy=mostInjuredAndVisiblyNear(world,hero,Integer.MAX_VALUE,enemies);
                if (weakestEnemy!=null && hero.getAbility(AbilityName.SENTRY_RAY).isReady())
                    world.castAbility(hero,AbilityName.SENTRY_RAY,weakestEnemy.getCurrentCell());
                else{
                    weakestEnemy=mostInjuredAndVisiblyNear(world,hero,7,enemies);
                    if (weakestEnemy!=null && hero.getAbility(AbilityName.SENTRY_ATTACK).isReady())
                        world.castAbility(hero,AbilityName.SENTRY_ATTACK,hero.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.GUARDIAN){
                Hero mostInjured=mostInjuredAndNear(world,hero,4,toBeHealed);
                if (mostInjured!=null && hero.getAbility(AbilityName.GUARDIAN_FORTIFY).isReady()){
                    world.castAbility(hero,AbilityName.GUARDIAN_FORTIFY,mostInjured.getCurrentCell());
                }else {
                    Hero weakestEnemy = mostInjuredAndNear(world, hero,1, enemies);
                    if (weakestEnemy!=null && hero.getAbility(AbilityName.GUARDIAN_ATTACK).isReady())
                        world.castAbility(hero,AbilityName.GUARDIAN_ATTACK,weakestEnemy.getCurrentCell());
                    //else maybe dodge
                }
            }
        }
    }

    private Hero mostInjuredAndNear(World world,Hero applier,int maxDistance,List<Hero> heroes){
        if (heroes.size()==0)
            return null;
        Hero selected=null;
        for(Hero hero:heroes){
            if (selected==null){
                if (world.manhattanDistance(applier.getCurrentCell(),hero.getCurrentCell())<=maxDistance)
                    selected=hero;
            }
            else if (hero.getCurrentHP()<selected.getCurrentHP() && world.manhattanDistance(applier.getCurrentCell(),hero.getCurrentCell())<=maxDistance)
                selected=hero;
        }
        return selected;
    }
    private Hero mostInjuredAndVisiblyNear(World world,Hero applier,int maxDistance,List<Hero> heroes){
        if (heroes.size()==0)
            return null;
        Hero selected=null;
        for(Hero hero:heroes){
            if (selected==null){
                if (world.manhattanDistance(applier.getCurrentCell(),hero.getCurrentCell())<=maxDistance && world.isInVision(applier.getCurrentCell(),hero.getCurrentCell()))
                    selected=hero;
            }
            else if (hero.getCurrentHP()<selected.getCurrentHP() && world.manhattanDistance(applier.getCurrentCell(),hero.getCurrentCell())<=maxDistance && world.isInVision(applier.getCurrentCell(),hero.getCurrentCell()))
                selected=hero;
        }
        return selected;
    }
}
