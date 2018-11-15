package ryley.wordsprint2;

public class WarmUpSkips {

    /*
    Example warmup process:
    Three stages
    Each stage 4-n
    skip 3 times, next, skip 3 times next...
    2, next, 2
    normal
     */

    private int stage;
    private int skips;
    private final int skipsMax;

    WarmUpSkips(){
        stage = 2;
        skipsMax = 2;
        skips = skipsMax;
    }

    WarmUpSkips(int stages, int skips){
        this.skipsMax = skips;
        this.skips = skipsMax;
        this.stage = stages;
    }

    public boolean shouldSkip(){
        if(stage >= 1){
            if(skips >= 1){
                skips--;
                return true;
            }
            else{
                stage--;
                skips = skipsMax;
                return false;
            }
        }
        else{
            return false;
        }
    }
}
