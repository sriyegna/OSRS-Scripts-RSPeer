import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

//Have runepouch with ourania tp runes
//Have 3 pouches, small, med, large

@ScriptMeta(developer = "Sri", name = "Ourania2", desc = "Runecrafting")
public class Ourania2 extends Script {

    private int TotalSleepTime = 0;
    private int sleepCount = 0;

//    private String state = "walkToPrayerAltar";
    private String state = "walkToPrayerAltar";
    private int count = 0;

    boolean smallPouchEmpty = true;
    boolean medPouchEmpty = true;
    boolean largePouchEmpty = true;

    private Area ouraniaTeleport = Area.rectangular(new Position(2466, 3242), new Position(2469, 3248));
    private Area prayerAltar = Area.rectangular(new Position(2454, 3235), new Position(2455, 3230));
    private Area insideOurania = Area.rectangular(new Position(3011, 5619), new Position(3021, 5629));
//    private Area runecraftingAltar = Area.rectangular(new Position(3055, 5575), new Position(3062, 5582));
    private Area runecraftingAltar = Area.rectangular(new Position(3056, 5577), new Position(3056, 5581));


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

        TotalSleepTime = TotalSleepTime + sleepTime;
        sleepCount++;
    }

    @Override
    public int loop() {


        if (Health.getCurrent() < 20) {
            Game.logout();
            return -1;
        }

        if (Movement.getRunEnergy() >= getRand(70, 100)) {
            if (!Movement.isRunEnabled()) {
                Movement.toggleRun(true);
            }
        }

        if (state.equals("walkToPrayerAltar")) {
            if (!prayerAltar.contains(Players.getLocal().getTarget())) {
                Movement.walkTo(Random.nextElement(prayerAltar.getTiles()));
                randomWait(1700, 2600);
            }
            //Time.sleepUntil(() -> prayerAltar.contains(Players.getLocal().getPosition()), getRand(15000, 25000));

            if (prayerAltar.contains(Players.getLocal().getPosition())) {
                randomWait(600, 1000);
                state = "climbLadder";
            }
        }

        if (state.equals("climbLadder")) {
            SceneObject ladder = SceneObjects.getNearest(29635);

            if (ladder != null) {
                ladder.interact("Climb");
                //Time.sleepUntil(() -> insideOurania.contains(Players.getLocal().getPosition()), getRand(5000, 15000));
            }

            if (insideOurania.contains(Players.getLocal().getPosition())) {
                randomWait(700, 1200);
                state = "bankPhase1";
            }
        }

        if (state.equals("bankPhase1")) {
            Npc eniola = Npcs.getNearest(7417);

            if (eniola != null && !Bank.isOpen()) {
                eniola.interact("Bank");
                Time.sleepUntil(() -> Bank.isOpen(), getRand(7000, 15000));
                randomWait(300, 600);

                if (Bank.isOpen()) {
                    Bank.depositInventory();
                    Time.sleepUntil(() -> Inventory.containsOnly(i -> i.getName().contains("pouch")), getRand(8000, 16000));
                    randomWait(500, 800);

                    Bank.withdrawAll(7936);
                    if (Time.sleepUntil(() -> Inventory.contains(7936), getRand(4000, 12000))) {
                        Log.info("Bank Phase 1 essence withdrawn");
                    };
                    randomWait(500, 1000);
                    Bank.close();
                    randomWait(500, 1000);
                    state = "fillPouches";
                }
            }
        }

        if (state.equals("fillPouches")) {
            Log.info("State: " + state);
            if (Inventory.getCount(7936) > 6) {
                Log.info("Count > 6");
                //Fill pouches
                Item smallPouch = Inventory.getFirst(5509);
                Item medPouch = Inventory.getFirst(5510);
                Item largePouch = Inventory.getFirst(5512);

                if (smallPouchEmpty && smallPouch != null) {
                    int pureEssenceCount = Inventory.getCount(7936);
                    smallPouch.interact("Fill");
                    Log.info("Interacted sm pouch");
                    int finalPureEssenceCount = pureEssenceCount;
                    if(Time.sleepUntil(() -> Inventory.getCount(7936) < finalPureEssenceCount, getRand(5000, 10000))) {
                        Log.info("Small pouch filled");
                        smallPouchEmpty = false;
                    }
                    randomWait(400, 800);
                }

                if (medPouchEmpty && medPouch != null) {
                    int pureEssenceCount = Inventory.getCount(7936);
                    medPouch.interact("Fill");
                    int finalPureEssenceCount1 = pureEssenceCount;
                    if (Time.sleepUntil(() -> Inventory.getCount(7936) < finalPureEssenceCount1, getRand(5000, 10000))) {
                        Log.info("Med pouch filled");
                        medPouchEmpty = false;
                    }
                    randomWait(400, 800);
                }

                if (largePouchEmpty && largePouch != null) {
                    int pureEssenceCount = Inventory.getCount(7936);
                    largePouch.interact("Fill");
                    int finalPureEssenceCount2 = pureEssenceCount;
                    if (Time.sleepUntil(() -> Inventory.getCount(7936) < finalPureEssenceCount2, getRand(5000, 10000))) {
                        Log.info("Large pouch filled");
                        largePouchEmpty = false;
                    }
                    randomWait(400, 800);
                }
            }

            if (Inventory.contains(5513) || Inventory.contains(5511)) {
                sound();
                state = "repairPouches";
            }
            else if (!smallPouchEmpty && !medPouchEmpty && !largePouchEmpty) {
                state = "bankPhase2";
            }
        }

        if (state.equals("repairPouches")) {
            Log.fine("State: " + state);
            if (Inventory.contains(5513) || Inventory.contains(5511)) {
                //Write code to NPC contact and repair pouches
                if (!Inventory.contains(564) || !Inventory.contains(556)) {
                    Log.fine("Open bank");
                    Npc eniola = Npcs.getNearest(7417);

                    if (eniola != null && !Bank.isOpen()) {
                        eniola.interact("Bank");
                    }
                    Time.sleepUntil(() -> Bank.isOpen(), getRand(5000, 10000));
                    if (!Inventory.contains(564)) {
                        randomWait(500, 1000);
                        Bank.withdraw(564, 1);
                    }
                    if (Time.sleepUntil(() -> Inventory.contains(564), getRand(5000, 10000))) {
                        Log.info("Cosmic rune withdrawn");
                        randomWait(400, 800);
                        if (!Inventory.contains(556)) {
                            Bank.withdraw(556, 2);
                            if (Time.sleepUntil(() -> Inventory.contains(556), getRand(5000, 10000))) {
                                Log.info("Air runes withdrawn");
                                randomWait(400, 800);
                                Bank.close();
                                randomWait(500, 1000);
                            }
                        }
                    }
                }

                if (Inventory.contains(564) && Inventory.contains(556)) {
                    Log.fine("Contacting NPC");
                    //Contact NPC
                    if (Magic.canCast(Spell.Lunar.NPC_CONTACT)) {
                        Log.fine("Can cast spell");
                        Magic.cast(Spell.Lunar.NPC_CONTACT);
                        if (Time.sleepUntil(() -> Interfaces.isOpen(75), getRand(5000, 10000))) {
                            Log.fine("NPC Contact interface is open");
                            randomWait(500, 1000);
                            Interfaces.getComponent(75, 12).click();
                            if (Time.sleepUntil(() -> !Players.getLocal().isAnimating() && Dialog.isOpen(), getRand(5000, 10000))) {
                                Dialog.processContinue();
                                randomWait(500, 1000);
                                Dialog.process(1);
                                randomWait(500, 1000);
                                Dialog.processContinue();
                                randomWait(500, 1000);
                                Dialog.processContinue();
                                randomWait(500, 1000);

                            }
                        }
                    }
                }
            }

            if (Inventory.contains(5510) && Inventory.contains(5512)) {
                state = "fillPouches";
            }
        }

        if (state.equals("bankPhase2")) {
            Npc eniola = Npcs.getNearest(7417);

            if (eniola != null) {
                eniola.interact("Bank");
                Time.sleepUntil(() -> Bank.isOpen(), getRand(7000, 15000));
                randomWait(300, 600);

                if (Bank.isOpen()) {
                    Bank.withdrawAll(7936);
                    Time.sleepUntil(() -> Inventory.contains(7936), getRand(4000, 12000));
                    randomWait(500, 1000);
                }
            }

            if (Inventory.isFull()) {
                state = "walkToRunecraftingAltar";
            }
        }

        if (state.equals("walkToRunecraftingAltar")) {
            if (!runecraftingAltar.contains(Players.getLocal().getTarget())) {
                Movement.walkTo(Random.nextElement(runecraftingAltar.getTiles()));
                if (Movement.isRunEnabled()) {
                    randomWait(1600, 2700);
                }
                else {
                    randomWait(3200, 5000);
                }
            }

            if (runecraftingAltar.contains(Players.getLocal().getPosition())) {
                randomWait(800, 1300);
                state = "craftRunes";
            }
        }

        //Work from here

        if (state.equals("craftRunes")) {
            SceneObject altar = SceneObjects.getNearest(29631);

            if (altar != null && Inventory.contains(7936)) {
                altar.interact("Craft-rune");
                Time.sleepUntil(() -> !Inventory.contains(7936) && !Players.getLocal().isAnimating(), getRand(5000, 7000));
                randomWait(1000, 2000);
            }

            if (!Inventory.contains(7936)) {
                state = "emptyPouchesSmallMed";
            }
        }

        if (state.equals("emptyPouchesSmallMed")) {
            if (!smallPouchEmpty) {
                int pureEssenceCount = Inventory.getCount(7936);
                Item smallPouch = Inventory.getFirst(5509);
                smallPouch.interact("Empty");
                int finalPureEssenceCount = pureEssenceCount;
                if (Time.sleepUntil(() -> Inventory.getCount(7936) > finalPureEssenceCount, getRand(2000, 4000))) {
                    Log.info("Small pouch emptied");
                    smallPouchEmpty = true;
                }
                randomWait(400, 800);
            }

            if (!medPouchEmpty) {
                int pureEssenceCount = Inventory.getCount(7936);
                Item medPouch = Inventory.getFirst(5510);
                medPouch.interact("Empty");
                int finalPureEssenceCount1 = pureEssenceCount;
                if (Time.sleepUntil(() -> Inventory.getCount(7936) > finalPureEssenceCount1, getRand(2000, 4000))) {
                    Log.info("Med pouch emptied");
                    medPouchEmpty = true;
                }
                randomWait(400, 800);

                if (Inventory.contains(7936) && smallPouchEmpty && medPouchEmpty) {
                    state = "craftRunes2";
                }
            }
        }

        if (state.equals("craftRunes2")) {
            SceneObject altar = SceneObjects.getNearest(29631);

            if (altar != null && Inventory.contains(7936)) {
                altar.interact("Craft-rune");
                Time.sleepUntil(() -> !Inventory.contains(7936) && !Players.getLocal().isAnimating(), getRand(2000, 4000));
                randomWait(1000, 2000);
            }

            if (!Inventory.contains(7936)) {
                state = "emptyPouchesLarge";
            }
        }

        if (state.equals("emptyPouchesLarge")) {
            if (!largePouchEmpty) {
                int pureEssenceCount = Inventory.getCount(7936);
                Item largePouch = Inventory.getFirst(5512);
                largePouch.interact("Empty");
                int finalPureEssenceCount1 = pureEssenceCount;
                if (Time.sleepUntil(() -> Inventory.getCount(7936) > finalPureEssenceCount1, getRand(5000, 10000))) {
                    Log.info("Large pouch emptied");
                    largePouchEmpty = true;
                }
                randomWait(400, 800);

                if (Inventory.contains(7936) && largePouchEmpty) {
                    state = "craftRunes3";
                }
            }
        }

        if (state.equals("craftRunes3")) {
            SceneObject altar = SceneObjects.getNearest(29631);

            if (altar != null && Inventory.contains(7936)) {
                altar.interact("Craft-rune");
                Time.sleepUntil(() -> !Inventory.contains(7936) && !Players.getLocal().isAnimating(), getRand(5000, 7000));
                randomWait(400, 800);
            }

            if (!Inventory.contains(7936)) {
                state = "tpOurania";
            }
        }

        if (state.equals("tpOurania")) {
            if (ouraniaTeleport.contains(Players.getLocal().getPosition())) {
                state = "walkToPrayerAltar";
                count++;
                Log.fine("Run #: " + count);
            }
            else if (Magic.canCast(Spell.Lunar.OURANIA_TELEPORT)) {
                Magic.cast(Spell.Lunar.OURANIA_TELEPORT);
                Time.sleepUntil(() -> ouraniaTeleport.contains(Players.getLocal().getPosition()), getRand(7000, 13000));
                randomWait(600, 900);
            }


        }

        return 0;
    }
}
