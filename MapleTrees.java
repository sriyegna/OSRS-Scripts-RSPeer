import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Maple Trees", desc = "Woodcutting")
public class MapleTrees extends Script {

    private static int MAPLE_TREE = 10832;
    private static Area MAPLE_TREE_AREA = Area.rectangular(new Position(2716, 3501), new Position(2731, 3495));
    private String state = "chop";

    private int teaksCut = 0;

    private int lowDelta = 60;
    private int highDelta = 190;

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

        if (state.equals("chop")) {
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                if (Inventory.isFull()) {
                    state = "goToBank";
                } else {
                    SceneObject tree = Random.nextElement(SceneObjects.getLoaded(t -> t.getId() == MAPLE_TREE && MAPLE_TREE_AREA.contains(t.getPosition())));
//                    SceneObject tree = SceneObjects.getNearest(t -> t.getId() == MAPLE_TREE && MAPLE_TREE_AREA.contains(t.getPosition()));
                    if (tree != null) {
                        randomWait(500, 1400);
                        tree.interact("Chop down");
                    }
                }
            }
        }

        if (state.equals("goToBank")) {
            if (!Inventory.isEmpty()) {
                SceneObject bank = Random.nextElement(SceneObjects.getLoaded(b -> b.getId() == 25808));
                bank.click();
                Time.sleepUntil(() -> Bank.isOpen(), getRand(10000, 15000));
                if (Bank.isOpen()) {
                    Bank.depositInventory();
                    randomWait(400, 900);
                }
            } else {
                teaksCut = teaksCut + 28;
                Log.info("Teaks cut: " + teaksCut);
                state = "chop";
            }
        }

        return 0;
    }
}
