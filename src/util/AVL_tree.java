package util;



import java.util.ArrayList;

import java.lang.Math;
import java.util.List;

/**
 * Created by BarikBin_user on 1/25/2017.
 */

public class AVL_tree<T extends Comparable<T>> {
    private AVL_node root;
    private int count=0;
    private class AVL_node {
        AVL_node r_node;
        AVL_node l_node;
        AVL_node father;
        T data;
        int Height;
        AVL_node(){
            r_node=null;
            l_node=null;
            father=null;
            data=null;
            Height=1;
        }
        int Balance(){
            if (r_node==null){
                if (l_node==null)
                    return 0;
                else
                    return -1;
            }
            else {
                if (l_node==null)
                    return 1;
                else if(r_node.Height>l_node.Height)
                    return 1;
                else if (r_node.Height<l_node.Height)
                    return -1;
                else return 0;
            }
        }
        void update_height(){
            if (r_node==null && l_node==null)
                Height=1;
            else if (r_node==null)
                Height=l_node.Height+1;
            else if (l_node==null)
                Height=r_node.Height+1;
            else
                Height=Math.max(r_node.Height,l_node.Height)+1;
        }
    }
    public void update(T b){
        AVL_node bb= _search(b,root);
        bb.data=b;
    }
    public void delete(T I){
        _delete(_search(I,root));
    }
    public AVL_tree(T[] x){
        for (T t:x){
            add(t);
        }
    }
    public AVL_tree(){
        root=null;
    }
    public ArrayList<T> in_order_traversal(){
        if (root==null)
            return new ArrayList<T> ();
        ArrayList<T> L=in_ord_trav(root.l_node);
        L.add(root.data);
        L.addAll(in_ord_trav(root.r_node));
        return L;
    }
    public int getCount(){
        return count;
    }
    private ArrayList<T> in_ord_trav(AVL_node x){
        if (x==null){
            return new ArrayList<>();
        }
        ArrayList<T> L=in_ord_trav(x.l_node);
        L.add(x.data);
        L.addAll(in_ord_trav(x.r_node));
        return L;
    }
    /*public ArrayList<T> in_ord_search(String query){
        return in_ord_search(root,query);
    }
    private ArrayList<T> in_ord_search(AVL_node x,String query){
        if (x==null){
            return new ArrayList<>();
        }
        ArrayList<Book> L=new ArrayList<>();
        if (x.book.name.contains(query) || x.book.author.contains(query) || x.book.publisher.contains(query))
            L.add(x.book);
        L.addAll(in_ord_search(x.l_node,query));
        L.addAll(in_ord_search(x.r_node,query));
        return L;
    }*/
    public T getMax(){
        if (root==null)
            return null;
        AVL_node tempMax=root;
        while (tempMax.r_node!=null)
            tempMax=tempMax.r_node;
        return tempMax.data;
    }
    public boolean exist(T key){
        return this.search(key)!=null;
    }
    public T search(T key){
        AVL_node b=_search(key,root);
        if (b!=null)
            return b.data;
        else
            return null;
    }
    private AVL_node _search(T isbn,AVL_node x){
        if (x==null)
            return null;
        if(x.data.equals(isbn))
            return x;
        else if (x.data.compareTo(isbn)<0) {
                return _search(isbn, x.r_node);
        }
        else
            return _search(isbn,x.l_node);
    }
    public void addAll(List<T> items){
        for (T t:items)
            add(t);
    }
    public void add(T b){
        AVL_node new_b=new AVL_node();
        new_b.data=b;
        if (root==null){
            root=new_b;
            root.Height=1;
        }
        else
            push_start_from(root,new_b,false);
        count++;
    }
    public boolean addIfNotDuplicate(T b){
        AVL_node new_b=new AVL_node();
        new_b.data=b;
        if (root==null){
            root=new_b;
            root.Height=1;
            count++;
            return true;
        }
        else if(push_start_from(root,new_b,true))
        {
            count++;
            return true;
        }
        return false;
    }
    void _delete(AVL_node x){
        if (x==null)
            return;
        AVL_node t=x.l_node;
        if(t!=null){
            while (t.r_node!=null){
                t=t.r_node;
            }
            AVL_node f=t.father;
            if (t.father !=x)
                t.father.r_node=t.l_node;
            if(t.l_node!= null && t.father!=x){
                t.l_node.father=t.father;
            }
            if (x!=root){
                if (x==x.father.r_node){
                    x.father.r_node=t;
                }
                else{
                    x.father.l_node=t;
                }
            }
            t.father=x.father;
            if (x.l_node!=t)
                t.l_node=x.l_node;
            t.r_node=x.r_node;
            if (x.r_node!=null)
                x.r_node.father=t;
            if (x.l_node!=t)
                x.l_node.father=t;
            if(f==x && f!=root)
                f=f.father;
            AVL_node temp=f;
            if (f!=root){
            while(temp!=null){
                temp.update_height();
                temp=temp.father;
            }
            }
            if (x==root){
                root=t;
            }
            if (f!=x)
                check_being_AVL(f);
            count--;
        }
        else{
            t=x.r_node;
            if (t==null){
                if (x==root){
                    root=null;
                    count--;
                    return;
                }
                else {
                    if(x.father.l_node==x){
                        x.father.l_node=null;
                    }
                    else{
                        x.father.r_node=null;
                    }
                    AVL_node temp=x.father;
                    while (temp!=null)
                    {
                        temp.update_height();
                        temp=temp.father;
                    }
                    //x.father.update_height();
                    check_being_AVL(x.father);
                    x.father=null;
                    count--;
                    //System.gc();
                    return;
                }
            }
            while (t.l_node!=null){
                t=t.l_node;
            }
            AVL_node f=t.father;
            if (t.father!=x)
                t.father.l_node=t.r_node;
            if(t.r_node!= null && t.father!=x){
                t.r_node.father=t.father;
            }
            if (x!=root){
                if (x==x.father.r_node){
                    x.father.r_node=t;
                }
                else{
                    x.father.l_node=t;
                }
            }
            t.father=x.father;
            if (x.r_node!=t)
                t.r_node=x.r_node;
            if (x.r_node!=t)
                x.r_node.father=t;
            if (x==root)
                root=t;
            if(f==x && f!=root)
                f=f.father;
            AVL_node temp=f;
            if (root!=t){
            while (temp!=null){
                temp.update_height();
                temp=temp.father;
            }
            }
            //f.update_height();
            if (f!=x)
                check_being_AVL(f);
            count--;
        }
    }
    private boolean push_start_from(AVL_node x,AVL_node neww,boolean preventDuplicate){
        if(_push_start_from(x,neww,preventDuplicate)) {
            check_being_AVL(neww);
            return true;
        }
        return false;
    }
    private boolean _push_start_from(AVL_node x,AVL_node neww,boolean preventDuplicate){
        int res=x.data.compareTo(neww.data);
        if (res>0){
            if (x.l_node==null) {
                x.l_node = neww;
                neww.father=x;
            }
            else {
                if(_push_start_from(x.l_node, neww,preventDuplicate))
                {
                    x.update_height();
                    return true;
                }
                else return false;
            }

        }
        else if (res!=0 || !preventDuplicate){
            if (x.r_node==null)
            {
                x.r_node=neww;
                neww.father=x;
            }
            else {
                if(_push_start_from(x.r_node, neww,preventDuplicate)){
                    x.update_height();
                    return true;
                }
                else return false;
            }
        }
        else return false;
        x.update_height();
        return true;
    }
    private void L_rotate(AVL_node x) {
        x.r_node.father=x.father;
        if (x != root) {
            if (x == x.father.l_node)
                x.father.l_node = x.r_node;
            else
                x.father.r_node = x.r_node;
        }
        AVL_node t=x.r_node.l_node;
        x.r_node.l_node=x;
        x.father=x.r_node;
        x.r_node=t;
        if (t!=null)
            t.father=x;
        if (x==root)
            root=x.father;
        x.update_height();
        x.father.update_height();
    }
    private void R_rotate(AVL_node x){
        x.l_node.father=x.father;
        if (x!=root){
            if (x==x.father.l_node)
                x.father.l_node=x.l_node;
            else
                x.father.r_node=x.l_node;
        }
        AVL_node t=x.l_node.r_node;
        x.l_node.r_node=x;
        x.father=x.l_node;
        x.l_node=t;
        if (t!=null)
            t.father=x;
        if (x==root)
            root=x.father;
        x.update_height();
        x.father.update_height();
    }
    private void RL_rotate(AVL_node x){
        R_rotate(x.r_node);
        L_rotate(x);
    }
    private void LR_rotate(AVL_node x){
        L_rotate(x.l_node);
        R_rotate(x);
    }
    private void check_being_AVL(AVL_node x){
         AVL_node temp=x;
        while (temp != null){
            temp.update_height();
            if(temp.r_node==null && temp.l_node==null){
                temp=temp.father;
                continue;
            }
            if (temp.r_node==null){
                if (temp.l_node.Height>1){
                    if (temp.l_node.r_node!= null)
                        LR_rotate(temp);
                    else
                        R_rotate(temp);

                }
            }
            else if (temp.l_node==null){
                if (temp.r_node.Height>1){
                    if (temp.r_node.l_node!= null)
                        RL_rotate(temp);
                    else
                        L_rotate(temp);

                }
            }
            else {
                if (temp.r_node.Height-2>=temp.l_node.Height){
                    if (temp.r_node.Balance()==1)
                        L_rotate(temp);
                    else
                        RL_rotate(temp);
                }

                else if (temp.l_node.Height-2>=temp.r_node.Height)
                {
                    if (temp.l_node.Balance()==-1)
                        R_rotate(temp);
                    else
                        LR_rotate(temp);
                }

            }
            temp=temp.father;
        }
    }
}
