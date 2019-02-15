package ai.Qai;

import client.model.AbilityName;
import client.model.Direction;

import java.util.HashMap;
import java.util.Map;

public class Table {
    Map<State,Tuple<Direction,Double>> moveTable;
    //Map<State,Tuple<AbilityName,Double>> actionTable;
    public Table(){
        moveTable=new HashMap<>();
      //  actionTable=new HashMap<>();
    }
}
