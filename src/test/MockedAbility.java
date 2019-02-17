package test;

import client.model.Ability;
import client.model.AbilityName;

public class MockedAbility extends Ability {
    private int power;
    private AbilityName name;
    public MockedAbility(AbilityName name,int power){
        this.power=power;
        this.name=name;
    }

    @Override
    public AbilityName getName() {
        return name;
    }

    @Override
    public int getPower() {
        return power;
    }
}
