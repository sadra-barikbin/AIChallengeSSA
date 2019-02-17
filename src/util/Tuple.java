package util;

public class Tuple<k,v> {
    private k first;
    private v second;
    public k getFirst(){
        return first;
    }
    public v getSecond(){
        return second;
    }

    public void setFirst(k first) {
        this.first = first;
    }

    public void setSecond(v second) {
        this.second = second;
    }
    public Tuple(k first,v second){
        this.first=first;
        this.second=second;
    }
}
