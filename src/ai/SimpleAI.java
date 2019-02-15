package ai;

import client.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
made by Sadra
 **/
public class SimpleAI implements AbstractAI {
    private Random random = new Random();
    @Override
    public void preProcess(World world) {
        System.out.println("preProcess in simpleAI started");
    }

    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        world.pickHero(HeroName.values()[world.getCurrentTurn()]);
    }

    @Override
    public void moveTurn(World world) {
        System.out.println("move started");
        Hero[] heroes = world.getMyHeroes();
        Cell[] objZone=world.getMap().getObjectiveZone();
        for (Hero hero : heroes)
        {
            if (hero.getName()==HeroName.GUARDIAN && world.getMovePhaseNum()!=1)
                continue;
            if (hero.getName()==HeroName.BLASTER&& world.getMovePhaseNum()%4!=0)
                continue;
            if (hero.getName()==HeroName.SENTRY && world.getMovePhaseNum()%2!=0)
                continue;
            if (hero.getName()==HeroName.HEALER && world.getMovePhaseNum()%4!=0)
                continue;
            Direction[] goodPath=world.getPathMoveDirections(hero.getCurrentCell(),objZone[random.nextInt(objZone.length)]);
            if (goodPath.length==0)
                continue;
            Direction goodDir=goodPath[0];
            world.moveHero(hero, goodDir);
        }
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
                if (mostInjured!=null){
                    world.castAbility(hero,AbilityName.HEALER_HEAL,mostInjured.getCurrentCell());
                }else {
                    Hero weakestEnemy = mostInjuredAndNear(world, hero,4, enemies);
                    if (weakestEnemy!=null)
                        world.castAbility(hero,AbilityName.HEALER_ATTACK,weakestEnemy.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.BLASTER){
                Hero weakestEnemy=mostInjuredAndNear(world,hero,5,enemies);
                if (weakestEnemy!=null)
                    world.castAbility(hero,AbilityName.BLASTER_BOMB,weakestEnemy.getCurrentCell());
                else{
                    weakestEnemy=mostInjuredAndVisiblyNear(world,hero,4,enemies);
                    if (weakestEnemy!=null)
                        world.castAbility(hero,AbilityName.BLASTER_ATTACK,hero.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.SENTRY){
                Hero weakestEnemy=mostInjuredAndVisiblyNear(world,hero,Integer.MAX_VALUE,enemies);
                if (weakestEnemy!=null)
                    world.castAbility(hero,AbilityName.SENTRY_RAY,weakestEnemy.getCurrentCell());
                else{
                    weakestEnemy=mostInjuredAndVisiblyNear(world,hero,7,enemies);
                    if (weakestEnemy!=null)
                        world.castAbility(hero,AbilityName.SENTRY_ATTACK,hero.getCurrentCell());
                    //else maybe dodge
                }
            }
            else if(hero.getName()==HeroName.GUARDIAN){
                Hero mostInjured=mostInjuredAndNear(world,hero,4,toBeHealed);
                if (mostInjured!=null){
                    world.castAbility(hero,AbilityName.GUARDIAN_FORTIFY,mostInjured.getCurrentCell());
                }else {
                    Hero weakestEnemy = mostInjuredAndNear(world, hero,1, enemies);
                    if (weakestEnemy!=null)
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
