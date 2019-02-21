package ai.tactics;

import client.model.Cell;
import client.model.Hero;
import client.model.World;
public class DodgeTactic extends Tactic {
    private Cell to;
    @Override
    public void applyMove(Hero hero, World world) {

    }

    @Override
    public void applyAction(Hero hero, World world) {
        world.castAbility(hero, hero.getDodgeAbilities()[0], to);
    }

    public DodgeTactic(Cell to){
        name="DODGE";
        this.to=to;
    }
}
