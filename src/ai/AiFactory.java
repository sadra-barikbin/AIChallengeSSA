package ai;


//import test.TestAI;

public class AiFactory {
    private static AbstractAI ourInstance ;
    private static boolean modeSelected=false;
    public static AbstractAI getInstance(String AiMode) {
        modeSelected=true;
        if(ourInstance==null){
            switch (AiMode){
                case "complex":{
                    ourInstance=new ComplexAI();
                    break;
                }
                case "simple":{
                    ourInstance=new SimpleAI();
                    break;
                }
                case "structured-complex":{
                    ourInstance=new StructuredComplexAI();
                    break;
                }
                /*case "test":{
                    ourInstance=new TestAI();
                    break;
                }*/
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
