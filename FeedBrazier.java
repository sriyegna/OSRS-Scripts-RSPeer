import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Brazier", desc = "Firemaking")
public class FeedBrazier extends Script {

    private String state = "chopBruma";
    private int oldPlayerHp;

    private Position brazierPosition = new Position(1638, 3997);

    private int lowDelta = 80;
    private int highDelta = 170;

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

    public void continualCheckHealth() {
        for (int i = 0; i < 3; i++) {
            if (checkHealth()) {
                randomWait(600, 1300);
            };
        }
    }

    public boolean checkHealth() {
        int hp = Integer.parseInt(Interfaces.getComponent(160, 5).getText());
        int hpMin = (50 + getRand(0, 20));
//        Log.info("Player health: " + hp + " Min health: " + hpMin);
        if (hp < hpMin) {
            Log.info("Player health low");
            if (Inventory.contains(i -> i.getName().contains("Saradomin"))) {
                Log.info("Drinking brew");
                Item brew = Inventory.getFirst(i -> i.getName().contains("Saradomin"));
                brew.interact("Drink");
                randomWait(500, 1000);
                return true;
            }
        }
        return false;
    }

    @Override
    public int loop() {

        SceneObject brazier = SceneObjects.getNearest(29314);
        Log.info("Feeding Brazier");
        brazier.interact("Feed");
        randomWait(600, 1200);

        return -1;
    }
}
