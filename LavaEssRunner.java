import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
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

@ScriptMeta(developer = "Sri", name = "LavaEssenceRunner", desc = "Runecrafting")
public class LavaEssRunner extends Script {

    private static Area FIRE_ALTAR_AREA = Area.rectangular(new Position(3308, 3259), new Position(3318, 3250));
    private static Area DUEL_BANK_AREA = Area.rectangular(new Position(3381, 3272), new Position(3384, 3267));
    private static Area FIRE_RUINS_AREA = Area.rectangular(new Position(2569, 4853), new Position(2595, 4824));
    private static Position FIRE_ALTAR = new Position(3312, 3254);
    private static int PURE_ESSENCE = 7936;
    private static String PLAYER_NAME = "lampwaterasp";

    private String state = "atBank";


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

        if (state.equals("atBank")) {
            Bank.open();
            if (Time.sleepUntil(() -> Bank.isOpen(), getRand(5000, 10000))) {
                randomWait(500, 1000);
                Bank.withdrawAll(PURE_ESSENCE);
                randomWait(500, 1000);

                if (Inventory.contains(PURE_ESSENCE)) {
                    Bank.close();
                    randomWait(500, 1000);
                    state = "toMysteriousRuins";
                }
            }
        }

        if (state.equals("toMysteriousRuins")) {
            if (Players.getLocal().getPosition().distance(FIRE_ALTAR) < 7) {
                SceneObject mysteriousRuins = SceneObjects.getNearest(34817);
                if (mysteriousRuins != null) {
                    mysteriousRuins.interact("Enter");
                    if (Time.sleepUntil(() -> FIRE_RUINS_AREA.contains(Players.getLocal().getPosition()), getRand(5000, 10000))) {
                        Log.fine("In Fire Ruins");
                        state = "inFireRuins";
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
            Player[] players = Players.getLoaded(p -> p.getName().equals(PLAYER_NAME));
            if (players.length == 1) {
                Player sri = players[0];

                sri.interact("Trade with");
                if (Time.sleepUntil(() -> Trade.isOpen(), getRand(45000, 75000))) {
                    randomWait(500, 1000);
                    Trade.offerAll(PURE_ESSENCE);
                    if (Time.sleepUntil(() -> !Inventory.contains(PURE_ESSENCE), getRand(5000, 10000))) {
                        randomWait(500, 1000);
                        Trade.accept();
                        if (Time.sleepUntil(() -> Trade.hasOtherAccepted(), getRand(45000, 75000))) {
                            randomWait(500, 1000);
                            Trade.accept();
                            if (!Inventory.contains(PURE_ESSENCE)) {
                                state = "exitRuins";
                            }
                        }
                    }

                }

            }
        }

        if (state.equals("exitRuins")) {
            SceneObject portal = SceneObjects.getNearest(34752);

            if (portal != null) {
                portal.interact("Use");
                if (Time.sleepUntil(() -> FIRE_ALTAR_AREA.contains(Players.getLocal().getPosition()), getRand(5000, 10000))) {
                    randomWait(500, 1000);
                    state = "goToBank";
                }
            }
        }

        if (state.equals("goToBank")) {
            if (DUEL_BANK_AREA.contains(Players.getLocal().getPosition())) {
                state = "atBank";
            }
            else {
                Movement.walkTo(Random.nextElement(DUEL_BANK_AREA.getTiles()));
                if (Movement.isRunEnabled()) {
                    randomWait(1600, 2700);
                }
                else {
                    randomWait(3200, 5000);
                }
            }
        }

        return 0;
    }
}
