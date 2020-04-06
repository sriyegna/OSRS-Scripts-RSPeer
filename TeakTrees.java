import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;


@ScriptMeta(developer = "Sri", name = "Teak Trees", desc = "Woodcutting")
public class TeakTrees extends Script {
    private static int TEAK_TREE = 9036;
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

    int [] drop1 = new int[] {1, 5, 9, 13, 17, 21, 25, 2, 6, 10, 14, 18, 22, 26, 3, 7, 11, 15, 19, 23, 27, 4, 8, 12, 16, 20, 24, 28};
    int [] drop2 = new int [] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28};
    int [] drop3 = new int [] {1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16, 17, 21, 25, 18, 22, 26, 19, 23, 27, 20, 24, 28};

    public void randomDrop() {

    }



    @Override
    public int loop() {

        if (state.equals("chop")) {
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {


                if (Inventory.isFull()) {
                    state = "emptyInventory";
                }
                else {
                    SceneObject tree = SceneObjects.getNearest(TEAK_TREE);
                    if (tree != null) {
                        randomWait(500, 1400);
                        tree.interact("Chop down");
                    }
                }
            }
        }

        if (state.equals("emptyInventory")) {
            if (!Inventory.isEmpty()) {
                //!-- RANDOMIZE DROPPING PATTERN
                Item item = Inventory.getFirst(i -> i.getName() != "");
                if (item != null) {
                    randomWait(200, 500);
                    item.interact("Drop");
                }
            }
            else {
                teaksCut = teaksCut + 28;
                Log.info("Teaks cut: " + teaksCut);
                state = "chop";
            }
        }

        return 0;
    }
}
