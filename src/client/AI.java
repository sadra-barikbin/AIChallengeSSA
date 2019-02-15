package client;

import ai.AiFactory;
import client.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI
{



    public void preProcess(World world)
    {
        AiFactory.getInstance("simple").preProcess(world);
    }

    public void pickTurn(World world)
    {
        AiFactory.getInstance().pickTurn(world);
    }

    public void moveTurn(World world)
    {
        AiFactory.getInstance().moveTurn(world);
    }

    public void actionTurn(World world) {
        AiFactory.getInstance().actionTurn(world);
    }

}
