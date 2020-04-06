import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;

import java.util.Date;

@ScriptMeta(developer = "Sri", name = "Blast Furnace", desc = "Smithing")
public class BlastFurnace extends Script {

    private static final String CONVEYOR = "Conveyor belt";
    private static final String DISPENSER = "Bar dispenser";

    private static final int GOLD_BAR = 2357;
    private static final int GOLD_ORE = 444;
    private static final int GOLD_GAUNTLETS = 776;
    private static final int ICE_GLOVES = 1580;

    private static final Area DEPOSIT_AREA = Area.rectangular(new Position(1942, 4965), new Position(1937, 4969));
    private static final Area DISPENSER_AREA = Area.rectangular(new Position(1940, 4964), new Position(1939, 4962));

    private String state = "start";

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

    public void afterInteraction() {
        Time.sleep(getRand(500, 1000));
    }

    public void putGoldGloves() {
        if (!Equipment.contains(GOLD_GAUNTLETS)) {
            if (Random.low(0, 100) > 70) {
                Inventory.getFirst(GOLD_GAUNTLETS).interact("Wear");
            }
        }
    }

    public void forcePutGoldGloves() {
        if (!Equipment.contains(GOLD_GAUNTLETS)) {
            Inventory.getFirst(GOLD_GAUNTLETS).interact("Wear");
        }
    }

    public boolean hasAction(String[] actions, String action) {
        for(String s: actions) {
            if (s.equals(action)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public int loop() {

        if (state.equals("toConveyor")) {
            //Randomly put gold gloves
            putGoldGloves();
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                //Move to deposit area
                Movement.walkTo(Random.nextElement(DEPOSIT_AREA.getTiles()));
            }

            if (DEPOSIT_AREA.contains(Players.getLocal().getPosition())) {
                state = "depositConveyor";
            }
        }

        if (state.equals("depositConveyor")) {
            //putGoldGloves();
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                SceneObject conveyor_belt = SceneObjects.getNearest("Conveyor belt");
                conveyor_belt.interact("Put-ore-on");
            }

            if (Inventory.getCount(GOLD_ORE) == 0) {
                state = "toDispenser";
            }
        }

        if (state.equals("toDispenser")) {
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                //Move to dispenser area
                Movement.walkTo(Random.nextElement(DISPENSER_AREA.getTiles()));
            }
            if (hasAction(SceneObjects.getNearest("Bar dispenser").getActions(), "Take")) {
                if (!Equipment.contains(ICE_GLOVES)) {
                    Inventory.getFirst(ICE_GLOVES).interact("Wear");
                }
            }
            if (DISPENSER_AREA.contains(Players.getLocal().getPosition())) {
                state = "takeBars";
            }
        }

        if (state.equals("takeBars")) {
            if (hasAction(SceneObjects.getNearest("Bar dispenser").getActions(), "Take")) {
                if (!Equipment.contains(ICE_GLOVES)) {
                    Inventory.getFirst(ICE_GLOVES).interact("Wear");
                }
            }

            SceneObjects.getNearest("Bar dispenser").interact("Take");
            if (!hasAction(SceneObjects.getNearest("Bar dispenser").getActions(), "Take")) {
                state = "toBank";
            }
        }

        if (state.equals("toBank")) {
            if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                if (!Bank.isOpen()) {
                    Bank.open();
                }
            }

            if (Bank.isOpen()) {
                Bank.depositAll(GOLD_BAR);
                afterInteraction();
                Bank.withdrawAll(GOLD_ORE);
                afterInteraction();
                Bank.close();
                afterInteraction();
                state = "toConveyor";

            }
        }

        return 0;
    }
}
