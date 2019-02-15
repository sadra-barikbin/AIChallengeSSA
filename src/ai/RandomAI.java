package ai;

import client.model.*;


import java.util.Random;

public class RandomAI implements AbstractAI {
    private Random random = new Random();
    @Override
    public void actionTurn(World world) {
        System.out.println("action started");
        Hero[] heroes = world.getMyHeroes();
        Map map = world.getMap();
        for (Hero hero : heroes)
        {
            int row = random.nextInt(map.getRowNum());
            int column = random.nextInt(map.getColumnNum());

            world.castAbility(hero, hero.getAbilities()[random.nextInt(3)], row, column);
        }
    }

    @Override
    public void moveTurn(World world) {
        System.out.println("move started");
        Hero[] heroes = world.getMyHeroes();

        for (Hero hero : heroes)
        {
            world.moveHero(hero, Direction.values()[random.nextInt(4)]);
        }
    }

    @Override
    public void pickTurn(World world) {
        System.out.println("pick started");
        world.pickHero(HeroName.values()[world.getCurrentTurn()]);
    }

    @Override
    public void preProcess(World world) {
        System.out.println("pre process started");
    }
}
