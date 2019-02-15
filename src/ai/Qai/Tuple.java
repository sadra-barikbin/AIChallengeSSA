package ai.Qai;

public class Tuple<k,v> {
    private k t1;
    private v t2;
    public Tuple(k t1,v t2){
        this.t1=t1;
        this.t2=t2;
    }

    public k getT1() {
        return t1;
    }

    public v getT2() {
        return t2;
    }
}
