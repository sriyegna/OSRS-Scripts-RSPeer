import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.Random;

@ScriptMeta(developer = "Sri", name = "Ardy Knight Pickpocket", desc = "Pickpocket")
public class ArdyKnight extends Script {

    Player local;
    Random ran = new Random();
    int thievingXP;
    int count = 0;
    int avg = 0;

    public int getRand() {
        int highRand = org.rspeer.runetek.api.commons.math.Random.high(100, 500);
        int lowRand = org.rspeer.runetek.api.commons.math.Random.low(100, 500);
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
        thievingXP = Skills.getExperience(Skill.THIEVING);
        Log.fine("Start XP: " + thievingXP);
        super.onStart();
    }

    @Override
    public int loop() {
        local = Players.getLocal();

        if (!Equipment.contains("Dodgy necklace")) {
            if (!Inventory.contains("Dodgy necklace")) {
                Bank.open();
                Time.sleepUntil(() -> Bank.isOpen(), getRand());

                if (!Bank.contains("Monkfish") || !Bank.contains("Dodgy necklace")) {
                    return -1;
                }

                Bank.withdraw("Dodgy necklace", 3);
                Time.sleepUntil(() -> Inventory.contains("Dodgy necklace"), getRand());

                Bank.withdrawAll("Monkfish");
                Time.sleepUntil(() -> Inventory.isFull(), getRand());

                Bank.deposit("Monkfish", 1);
                Time.sleepUntil(() -> !Bank.isFull(), getRand());

            }

            Inventory.getFirst("Dodgy necklace").interact("Wear");
            Time.sleepUntil(() -> Equipment.contains("Dodgy necklace"), getRand());
        }

        if (!local.isAnimating() && !local.isMoving()) {
            if (local.getHealthPercent() < (ran.nextInt(30) + 20)) {
                Item food = Inventory.getFirst("Monkfish");
                int foodCount = Inventory.getCount("Monkfish");
                if (food != null) {
                    food.interact("Eat");
                    Time.sleepUntil(() -> (Inventory.getCount("Monkfish") == foodCount - 1) && !local.isAnimating(), getRand());
                }
                else {
                    Log.severe("Done with food. Killing");
                    return -1; // Kill bot
                }
            }
            else {
                int maxCoinPouches = ran.nextInt(8 + 1) + 20;
                Item coinPouch = Inventory.getFirst("Coin pouch");
                if (coinPouch != null && coinPouch.getStackSize() >= maxCoinPouches) {
                    System.out.println("Open coin pouches");
                    coinPouch.interact("Open-all");
                }
                else {
                    Npc knight = Npcs.getNearest("Knight of Ardougne");
                    if (local.getGraphic() != 245) {
                        System.out.println("Pickpocket");
                        knight.interact("Pickpocket");
                    }
                    else {
                        System.out.println("Stunned");
                    }
                }
            }
        }
        else {
            System.out.println("Animating or Moving");
        }

        int sleepTime = 250 + getRand();

        //Check average sleepTime
        avg = avg + sleepTime;
        count++;
        System.out.println("SleepTime: " + sleepTime);
        System.out.println("Average SleepTime: " + (avg/count));

        return sleepTime;
    }

    @Override
    public void onStop() {
        thievingXP = Skills.getExperience(Skill.THIEVING);
        Log.fine("Stop XP: " + thievingXP);
        super.onStop();
    }
}