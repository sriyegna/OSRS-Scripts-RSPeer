import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "DHide Crafter", desc = "Crafting")
public class dhideCrafter extends Script {

    private static int NEEDLE = 1733;
    private static int THREAD = 1734;
    private static int DRAGON_LEATHER = 2505;
    private static int DHIDE_BODY = 2499;


    private String state = "start";

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

    @Override
    public int loop() {

        if (state.equals("start")) {
            Bank.open();
            Time.sleepUntil(() -> Bank.isOpen(), getRand(5000, 10000));
            randomWait(500, 1100);
            if (Bank.isOpen()) {
                state = "bankOpen";
            }
        }

        if (state.equals("bankOpen")) {
            if (Inventory.contains(DHIDE_BODY)) {
                Bank.depositAll(DHIDE_BODY);
                Time.sleepUntil(() -> !Inventory.contains(DHIDE_BODY), getRand(4000, 11000));
                randomWait(300, 700);
            }

            if (Bank.contains(DRAGON_LEATHER)) {
                if (!Inventory.isFull()) {
                    Bank.withdrawAll(DRAGON_LEATHER);
                    Time.sleepUntil(() -> Inventory.contains(DRAGON_LEATHER), getRand(3000, 8000));
                    randomWait(500, 900);
                }
            }

            if (!Inventory.contains(DHIDE_BODY) && Inventory.contains(DRAGON_LEATHER)) {
                state = "craft";
                Bank.close();
                Time.sleepUntil(() -> Bank.isClosed(), getRand(3000, 6000));
                randomWait(600, 800);
            }
        }

        if (state.equals("craft")) {
            if (Inventory.contains(THREAD) && Inventory.contains(NEEDLE)) {
                Item needle = Inventory.getFirst(NEEDLE);
                Item dhidegreen = Inventory.getFirst(DRAGON_LEATHER);
                if (needle != null && Inventory.getCount(DRAGON_LEATHER) >= 3) {
                    needle.interact("Use");
                    Time.sleepUntil(() -> Inventory.isItemSelected(), getRand(2000, 4000));

                    if (Inventory.isItemSelected()) {
                        dhidegreen.interact("Use");
                    }
                    randomWait(800, 1300);

                    Time.sleepUntil(() -> Interfaces.isOpen(270), getRand(1300, 2700));
                    Time.sleep(getRand(600, 1000));

                    if (Interfaces.isOpen(270)) {
                        Log.fine("Creation interface open");
                        InterfaceComponent makeDHideBody = Interfaces.getComponent(270, 14);
                        makeDHideBody.click();

                        Time.sleepUntil(() -> Inventory.getCount(DRAGON_LEATHER) == 2, getRand(19500, 24300));
                        Time.sleep(getRand(400, 800));
                    }

                }
                else {
                    state = "start";
                }
            }
            else {
                Log.fine("No thread or needle. Quit");
                return -1;
            }
        }

        return 0;
    }
}
