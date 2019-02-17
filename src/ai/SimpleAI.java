package ai;

import ai.tactics.*;
import client.model.*;
import util.AVL_tree;
import util.Tuple;

import java.util.*;

/**
made by Sadra
 **/
public class SimpleAI implements AbstractAI {
    private Random random = new Random();
    private AVL_tree<Cell> walls;
    private AVL_tree<Cell> objectiveCells;
    private java.util.Map<Integer,Tactic> heroesTactics;
    @Override
    public void preProcess(World world) {
        System.out.println("preProcess in simpleAI started");
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
        if (world.getCurrentTurn()==4 && world.getMovePhaseNum()==0){
            heroesTactics=new HashMap<>();
            for (Hero h:heroes){
                heroesTactics.put(h.getId(),new GetToObjZoneTactic(objZone[random.nextInt(objZone.length)]));
            }
        }
        for (Hero hero : heroes)
        {
            Hero[] byTeamVisibleEnemies=world.getOppHeroes();
            Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>> dangersAndOpportunes=getDangersAndOpportunitiesForHero(hero,world,byTeamVisibleEnemies);
            Opportunity bestOpp=dangersAndOpportunes.getSecond().getMax();
            Danger mostSeriousDanger=dangersAndOpportunes.getFirst().getMax();
            if (bestOpp!=null) {
                if (bestOpp.type.getType() == AbilityType.DEFENSIVE)
                    heroesTactics.replace(hero.getId(), new DefenceTactic(bestOpp.type,bestOpp.in));
                else if (bestOpp.type.getType() == AbilityType.OFFENSIVE)
                    heroesTactics.replace(hero.getId(), new OffendTactic(bestOpp.type,bestOpp.in));
            }
            else if (mostSeriousDanger!=null){
                if (hero.getDodgeAbilities()[0].isReady() && world.getMovePhaseNum()==5)
                    heroesTactics.replace(hero.getId(),new DodgeTactic(hero.getCurrentCell()));
                else {
                    int escapeAimX = ((3 * hero.getCurrentCell().getColumn()) - mostSeriousDanger.from.getColumn()) / 2;
                    int escapeAimY = ((3 * hero.getCurrentCell().getRow()) - mostSeriousDanger.from.getRow()) / 2;
                    heroesTactics.replace(hero.getId(), new EscapeTactic(new Cell(escapeAimX, escapeAimY)));
                }
            }
            else if (!isCellIn(hero.getCurrentCell(),objectiveCells) )
                if (!(heroesTactics.get(hero.getId()) instanceof GetToObjZoneTactic))
                    heroesTactics.replace(hero.getId(),new GetToObjZoneTactic(world.getMap().getObjectiveZone()[random.nextInt(world.getMap().getObjectiveZone().length)]));
            else
                heroesTactics.replace(hero.getId(),new HoldOnTactic(hero.getCurrentCell()));
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
    private boolean isCellIn(Cell c,AVL_tree<Cell> set){
        return set.search(c)!=null;
    }
    private Tuple<AVL_tree<Danger>,AVL_tree<Opportunity>> getDangersAndOpportunitiesForHero(Hero hero,World world,Hero[] byTeamVisibleEnemies){
        AVL_tree<Danger> dangers=new AVL_tree<>();
        AVL_tree<Opportunity> opportunities=new AVL_tree<>();
        for (Hero enemy:byTeamVisibleEnemies){
            boolean heroSeeEnemy=world.isInVision(hero.getCurrentCell(),enemy.getCurrentCell());
            int distanceBtwMeAndEnemy=world.manhattanDistance(hero.getCurrentCell(),enemy.getCurrentCell());
            boolean canOffend=false;
            boolean canDodge=false;
            for (Ability dAbility:enemy.getDodgeAbilities()){
                if (dAbility.isReady()) {
                    canDodge = true;
                    break;
                }
            }
            for (Ability ability:enemy.getOffensiveAbilities()){
                if ((heroSeeEnemy||ability.isLobbing())&& ability.isReady() && ability.getRange()>=distanceBtwMeAndEnemy-(5-world.getMovePhaseNum())) {
                    dangers.add(new Danger(enemy.getCurrentCell(), ability,hero,world.getMovePhaseNum()));
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
                if (friend.getCurrentHP()<friend.getMaxHP()/5 && hero.getDefensiveAbilities()[0].getRange()<=world.manhattanDistance(hero.getCurrentCell(),friend.getCurrentCell())-(5-world.getMovePhaseNum()))
                    opportunities.add(new Opportunity(friend.getCurrentCell(),hero.getDefensiveAbilities()[0],hero,friend,world.getMovePhaseNum()));
            }
        }//defence guardian moonde
        return new Tuple<>(dangers,opportunities);
    }
}
