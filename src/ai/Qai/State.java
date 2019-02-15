package ai.Qai;

import client.model.*;

public class State {
    Direction objectiveZonePosition;
    int actionPointDivBy10;
    boolean isEnemyVisible;
    boolean isEnemyNear;
    boolean[] isWallNear;
    boolean healthInDanger;
    public static State updateState(State state,Hero hero, World world){
        state.actionPointDivBy10=world.getAP()/10;
        Hero[] visibleEnemies=world.getOppHeroes();
        state.isEnemyVisible=false;
        state.isEnemyNear=false;
        int nearCriterion=0;
        for (Ability ability:hero.getAbilities()){
            if (!ability.isLobbing())
                continue;
            if (ability.getRange()>nearCriterion)
                nearCriterion=ability.getRange();
        }
        for (Hero enemy:visibleEnemies){
            if (world.isInVision(hero.getCurrentCell(),enemy.getCurrentCell())){
                state.isEnemyVisible=true;
            }
            //if (world.manhattanDistance(hero.getCurrentCell(),enemy.getCurrentCell())<=nearCriterion)
            //    state.isEnemyNear=true;
        }
        state.healthInDanger=hero.getCurrentHP()<hero.getMaxHP()/5;
        return state;
    }
}
