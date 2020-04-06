import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Blackjack", desc = "Thieving")
public class BlackJack extends Script {

    private int lowDelta = 30;
    private int highDelta = 60;

    public int getRand(int low, int high) {
        int highRand = org.rspeer.runetek.api.commons.math.Random.high(low, high);
        int lowRand = org.rspeer.runetek.api.commons.math.Random.low(low, high);
        int rand;
        if (org.rspeer.runetek.api.commons.math.Random.nextInt(0, 100) < 50) {
            rand = lowRand;
        }
        else {
            rand = highRand;
        }
        return rand;
    }

    public void randomWait(int low, int high) {
        low = low + lowDelta;
        high = high + highDelta;
        int sleepTime = getRand(low, high);
        Time.sleep(sleepTime);
    }

    @Override
    public int loop() {
        if (Health.getCurrent() < 60) {
            Item jug = Inventory.getFirst(1993);
            if (jug != null) {
                jug.interact("Drink");
                randomWait(100, 300);
            }
        }


        Npc menaphite = Npcs.getNearest(n -> Players.getLocal().getPosition().distance(n.getPosition()) < 4 && n.getName().equals("Menaphite Thug"));

        if (menaphite.getAnimation() == -1 || menaphite.getAnimation() == 808) {
            if (Players.getLocal().getGraphic() != 245) {
                //Knock-Out
                randomWait(120, 170);
                menaphite.interact("Knock-Out");
                Time.sleepUntil(() -> Npcs.getNearest("Menaphite Thug").getAnimation() == 838, getRand(3000, 5000));
            }
            else {
                System.out.println("Stunned");
            }
        }
        else if (menaphite.getAnimation() == 838) {
            //Pickpocket
            randomWait(120, 250);
            menaphite.interact("Pickpocket");
            Time.sleepUntil(() -> !Players.getLocal().isAnimating(), getRand(3000, 5000));
        }
        else if (menaphite.getAnimation() == 395) {
            //Pickpocket
            //Sleep a little bit
            //If still attacking, kill it
            //When dead, play sound
            //Logout
            randomWait(80, 140);
            menaphite.interact("Pickpocket");
            if (!Time.sleepUntil(() -> Npcs.getNearest("Menaphite Thug").getAnimation() == -1, getRand(2000, 5000))) {
                Npcs.getNearest("Menaphite Thug").interact("Attack");
                if (Time.sleepUntil(() -> !Players.getLocal().isAnimating(), getRand(30000, 40000))) {
                    //play Sound
                    //Logout
                    Log.info("Logout");
                    return -1;
                }
            }
        }
        return 0;
    }
}
