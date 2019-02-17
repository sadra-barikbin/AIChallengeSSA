package test;

import client.model.Cell;
import client.model.Hero;

public class MockedHero extends Hero {
    private Cell currentCell;
    public MockedHero(Cell cell){
        super();
        this.currentCell=cell;
    }
    @Override
    public Cell getCurrentCell(){
        return this.currentCell;
    }


}
