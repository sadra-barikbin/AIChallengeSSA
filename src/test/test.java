package test;

import ai.Opportunity;
import client.model.Ability;
import client.model.AbilityName;
import client.model.Cell;
import org.junit.jupiter.api.Test;
import util.AVL_tree;

import java.util.Arrays;
import java.util.Random;

class test {
    @Test
    void t1(){
        AVL_tree<String> tree=new AVL_tree<>(new String[]{"a","b","c","ab","bd","ba"});
        System.out.println(tree.in_order_traversal());
        String b=tree.search("b");
        System.out.println(b);
    }
    @Test
    void t2(){
        AVL_tree<Cell> tree=new AVL_tree<>(new Cell[]{new Cell(1,1),new Cell(3,2),new Cell(4,1),new Cell(2,2),new Cell(3,4),new Cell(3,3)});
        System.out.println(tree.in_order_traversal());
        Cell b=tree.search(new Cell(3,2));
        System.out.println(b);
    }

    @Test
    void t4(){
        Cell[] a=new Cell[4];
        a[0]=new Cell(2,1);
        a[1]=new Cell(3,2);
        a[2]=new Cell(2,4);
        a[3]=new Cell(2,1);
        Arrays.sort(a);
        for (Cell c:a) {
            System.out.printf("%d %d\n",c.getRow(),c.getColumn());
        }

    }

}
