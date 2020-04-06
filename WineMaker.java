import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Wine Maker", desc = "Cooking")
public class WineMaker extends Script {

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

    @Override
    public void onStart() {
        Log.fine("This will be executed once on startup.");
        Bank.open();
        Time.sleepUntil(() -> Bank.isOpen(), getRand(3000, 4500));
        Time.sleep(getRand(800, 2200));
        if (!Inventory.isEmpty()) {
            Bank.depositInventory();
            Time.sleepUntil(() -> Inventory.isEmpty(), getRand(1600, 3200));
            Time.sleep(getRand(800, 2200));
        }
        super.onStart();
    }

    @Override
    public int loop() {
//        if (Bank.Tab.getOpen() != Bank.Tab.TAB_7) {
//            Bank.Tab.TAB_7.open();
//            Time.sleepUntil(() -> Bank.Tab.getOpen() == Bank.Tab.TAB_7, getRand(1700, 2800));
//            Time.sleep(getRand(800, 2200));
//        }

        if (Bank.contains("Jug of water") && Bank.contains("Grapes")) {
            Bank.withdraw("Jug of water", 14);
            Time.sleepUntil(() -> Inventory.contains("Jug of water"), 30000);
            Time.sleep(getRand(800, 2200));

            Bank.withdraw("Grapes", 14);
            Time.sleepUntil(() -> Inventory.contains("Grapes"), 30000);
            Time.sleep(getRand(800, 2200));

            Bank.close();
            Time.sleepUntil(() -> Bank.isClosed(), 4500);
            Time.sleep(getRand(800, 2200));

            Item lastJug, firstGrapes;
            if (Inventory.contains("Jug of water") && Inventory.contains("Grapes")) {
                lastJug = Inventory.getLast("Jug of water");
                firstGrapes = Inventory.getFirst("Grapes");

                if (lastJug != null && firstGrapes != null) {
                    lastJug.interact("Use");
                    Time.sleepUntil(() -> Inventory.isItemSelected(), getRand(1300, 2700));
                    Time.sleep(getRand(300, 600));


                    firstGrapes.interact("Use");
                    Time.sleepUntil(() -> Interfaces.isOpen(270), getRand(1300, 2700));
                    Time.sleep(getRand(600, 1000));

                    if (Interfaces.isOpen(270)) {
                        Log.fine("Creation interface open");
                        InterfaceComponent makeJugButton = Interfaces.getComponent(270, 14);
                        makeJugButton.click();

                        Time.sleepUntil(() -> Inventory.containsOnly("Unfermented wine"), getRand(19500, 24300));
                        Time.sleep(getRand(400, 800));

                        if (Inventory.containsOnly("Unfermented wine")) {
                            Bank.open();
                            Time.sleepUntil(() -> Bank.isOpen(), getRand(1100, 2200));
                            Time.sleep(getRand(500, 800));

                            Bank.depositInventory();
                            Time.sleepUntil(() -> Inventory.isEmpty(), getRand(400, 800));
                            Time.sleep(getRand(400, 1100));
                        }
                    }
                }


            }
        }
        else {
            return -1;
        }


        return 0;
    }
}
