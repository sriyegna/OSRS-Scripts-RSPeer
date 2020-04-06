import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

@ScriptMeta(developer = "Sri", name = "LavaEssenceRunnerDuelRing", desc = "Runecrafting Ring Runner")
public class LavaEssRunnerDuelRing extends Script {

    private static Area FIRE_ALTAR_AREA = Area.rectangular(new Position(3308, 3259), new Position(3318, 3250));
    private static Area CASTLE_WARS_AREA = Area.rectangular(new Position(2444, 3097), new Position(2438, 3082));
    private static Area DUEL_ARENA_TELEPORT_AREA = Area.rectangular(new Position(3318, 3240), new Position(3312, 3231));
    private static Area FIRE_RUINS_AREA = Area.rectangular(new Position(2569, 4853), new Position(2595, 4824));
    private static Position FIRE_ALTAR = new Position(3312, 3254);
    private static int PURE_ESSENCE = 7936;
    private static String PLAYER_NAME = "sri-1994";

    private String state = "start";


    public void sound() {
        try
        {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File("C:\\Notification.wav")));
            clip.start();
        }
        catch (Exception exc)
        {
            exc.printStackTrace(System.out);
        }
    }


    public int getRand(int low, int high) {
        int highRand = Random.high(low, high);
        int lowRand = Random.low(low, high);
        int rand;
        if (Random.nextInt(0, 100) < 50) {
            rand = lowRand;
        }
        else {
            rand = highRand;
        }
        return rand;
    }

    public void randomWait(int low, int high) {
        int sleepTime = getRand(low, high);
        Time.sleep(sleepTime);
    }


    @Override
    public int loop() {

        if (state.equals("start")) {
            Log.fine("At start");
            if (!CASTLE_WARS_AREA.contains(Players.getLocal().getPosition())) {
                if (Equipment.contains(i -> i.getName().contains("Ring of dueling"))) {
                    Equipment.getSlot(i -> i.getName().contains("Ring of dueling")).interact("Castle Wars");
                    if (Time.sleepUntil(() -> CASTLE_WARS_AREA.contains(Players.getLocal().getPosition()), getRand(5000, 10000))) {
                        state = "atBank";
                    }
                    else {
                        Game.logout();
                        return -1;
                    }
                }
                else {
                    Game.logout();
                    return -1;
                }
            }
        }

        if (Movement.getRunEnergy() >= getRand(70, 100)) {
            if (!Movement.isRunEnabled()) {
                Movement.toggleRun(true);
            }
        }

        if (state.equals("atBank")) {
            Log.fine("State: " + state);
            Bank.open();
            if (Time.sleepUntil(() -> Bank.isOpen(), getRand(5000, 10000))) {
                if (!Equipment.contains(i -> i.getName().contains("Ring of dueling"))) {
                    Bank.depositInventory();
                    Time.sleepUntil(() -> Inventory.isEmpty(), getRand(10000, 15000));
                    randomWait(500, 1000);
                    Bank.withdraw(2552, 1);
                }
                if (Inventory.contains(PURE_ESSENCE)) {
                    randomWait(500, 1000);
                    Bank.depositAll(PURE_ESSENCE);
                }
                randomWait(500, 1000);
                Bank.withdraw(PURE_ESSENCE, 25);
                Time.sleepUntil(() -> Inventory.contains(PURE_ESSENCE), getRand(5000, 10000));
                randomWait(500, 1000);

                if (Inventory.contains(PURE_ESSENCE)) {
                    Bank.close();
                    randomWait(500, 1000);
                    state = "tpToDuelArena";
                }
            }
        }

        if (state.equals("tpToDuelArena")) {
            Log.fine("State: " + state);
            if (!DUEL_ARENA_TELEPORT_AREA.contains(Players.getLocal().getPosition())) {
                if (Inventory.contains(i -> i.getName().contains("Ring of dueling"))) {
                    Item ring = Inventory.getFirst(i -> i.getName().contains("Ring of dueling"));
                    ring.interact("Wear");
                    randomWait(500, 1000);
                }
                Equipment.getSlot(i -> i.getName().contains("Ring of dueling")).interact("Duel Arena");
                if (Time.sleepUntil(() -> DUEL_ARENA_TELEPORT_AREA.contains(Players.getLocal().getPosition()), getRand(5000, 10000))) {
                    randomWait(1000, 2000);
                    state = "toMysteriousRuins";
                }
            }
            else {
                randomWait(2000, 4000);
                state = "toMysteriousRuins";
            }
        }

        if (state.equals("toMysteriousRuins")) {
            Log.fine("State: " + state);
            if (Players.getLocal().getPosition().distance(FIRE_ALTAR) < 10) {
                SceneObject mysteriousRuins = SceneObjects.getNearest(34817);
                if (mysteriousRuins != null) {
                    mysteriousRuins.interact("Enter");
                    if (Time.sleepUntil(() -> FIRE_RUINS_AREA.contains(Players.getLocal().getPosition()), getRand(10000, 15000))) {
                        Log.fine("In Fire Ruins");
                        state = "inFireRuins";
//                        state = "goToBank";
                    }
                }
            }
            else {
                Movement.walkTo(Random.nextElement(FIRE_ALTAR_AREA.getTiles()));
                if (Movement.isRunEnabled()) {
                    randomWait(1600, 2700);
                }
                else {
                    randomWait(3200, 5000);
                }
            }
        }

        if (state.equals("inFireRuins")) {
            Log.fine("State: " + state);
            Player[] players = Players.getLoaded(p -> p.getName().equals(PLAYER_NAME));
            if (players.length == 1) {
                Player sri = players[0];

                sri.interact("Trade with");
                sound();
                if (Time.sleepUntil(() -> Trade.isOpen(), getRand(10000, 30000))) {
                    randomWait(500, 1000);
                    Trade.offerAll(PURE_ESSENCE);
                    if (Time.sleepUntil(() -> Trade.isWaitingForMe(), getRand(10000, 20000))) {
                        randomWait(500, 1000);
                        Trade.accept();
                        if (Time.sleepUntil(() -> Trade.isOpen(true), getRand(10000, 20000))) {
                            randomWait(500, 1000);
                            Trade.accept();
                            if (Time.sleepUntil(() -> !Trade.isOpen(), getRand(10000, 20000))) {
                                if (!Inventory.contains(PURE_ESSENCE)) {
                                    randomWait(500, 1000);
                                    state = "goToBank";
                                }
                            }
                        }
                    }
                }

            }
        }


        if (state.equals("goToBank")) {
            if (CASTLE_WARS_AREA.contains(Players.getLocal().getPosition())) {
                state = "atBank";
            }
            else {
                Equipment.getSlot(i -> i.getName().contains("Ring of dueling")).interact("Castle Wars");
                if (Time.sleepUntil(() -> CASTLE_WARS_AREA.contains(Players.getLocal().getPosition()), getRand(5000, 10000))) {
                    randomWait(500, 1000);
                    state = "atBank";
                }
            }
        }

        return 0;
    }
}
