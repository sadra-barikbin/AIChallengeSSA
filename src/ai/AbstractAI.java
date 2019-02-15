package ai;

import client.model.World;

public interface AbstractAI {
    void preProcess(World world);
    void pickTurn(World world);
    void moveTurn(World world);
    void actionTurn(World world);
}
