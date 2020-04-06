import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Prayers;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

@ScriptMeta(developer = "Sri", name = "MMBurst", desc = "Magic")
public class MonkeyBurst extends Script {

    long startTime;

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
    public void onStart() {
        startTime = System.currentTimeMillis();
        super.onStart();
    }

    @Override
    public int loop() {

//        Npc nearest = Npcs.getNearest("Maniacal monkey");
//        Log.fine(nearest.getTarget().getName());
        if (Prayers.getPoints() < 37 + getRand(3, 12)) {
            Item pPot = Inventory.getFirst(i -> i.getName().contains("Prayer potion"));
            pPot.interact("Drink");
            randomWait(1000, 3000);
        }

        if (Inventory.getCount(i -> i.getName().contains("Prayer potion")) < 2 || Health.getCurrent() < 40) {
            sound();
            Time.sleep(2000);
            sound();
            Time.sleep(2000);
            sound();
            Time.sleep(2000);
            sound();

            Item teletab = Inventory.getFirst(8007);
            teletab.interact("Break");
            return -1;
        }


        //Aggro timer for 10 minutes
        if ((System.currentTimeMillis() - startTime) > 600000) {
            sound();
            Time.sleep(2000);
            sound();
            Time.sleep(2000);
            sound();
            Time.sleep(2000);
            sound();
            Log.fine("Aggro gone?");
            startTime = System.currentTimeMillis();
        }

        return 0;
    }
}
