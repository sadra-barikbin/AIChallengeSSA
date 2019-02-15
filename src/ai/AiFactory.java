package ai;

public class AiFactory {
    private static AbstractAI ourInstance ;
    private static boolean modeSelected=false;
    public static AbstractAI getInstance(String AiMode) {
        modeSelected=true;
        if(ourInstance==null){
            switch (AiMode){
                case "simple":{
                    ourInstance=new SimpleAI();
                    break;
                }
                default:{
                    ourInstance=new RandomAI();
                    break;
                }
            }
        }
        return ourInstance;
    }
    public static AbstractAI getInstance(){
        if (!modeSelected)
            throw new IllegalStateException("At very first getInstance(mode) must be called.");
        else
            return ourInstance;
    }

}
