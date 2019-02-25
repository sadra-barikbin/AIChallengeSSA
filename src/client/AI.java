package client;

import ai.AiFactory;
import client.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI
{

    private static final String STRUCTURED_COMPLEX="structured-complex";
    private static final String COMPLEX="complex";
    private static final String TEST="test";
    private static final String SIMPLE="simple";

    public void preProcess(World world)
    {
        AiFactory.getInstance(STRUCTURED_COMPLEX).preProcess(world);
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
