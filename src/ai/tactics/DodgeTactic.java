package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;
import util.AVL_tree;
import static ai.common.Functions.getGoodDodgeTarget;
public class DodgeTactic extends Tactic {

    @Override
    public void applyMove(Hero hero, World world) {

    }

    @Override
    public void applyAction(Hero hero, World world) {
        Cell bestOption=getGoodDodgeTarget(hero,world);
        if (bestOption!=null)
            world.castAbility(hero, hero.getDodgeAbilities()[0], bestOption);
    }

    public DodgeTactic(){
        name="DODGE";
    }
}
