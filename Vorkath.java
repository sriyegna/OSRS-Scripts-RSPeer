import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@ScriptMeta(developer = "Sri", name = "Vorkath", desc = "Vorkath")
public class Vorkath extends Script {
    File output;
    FileWriter writer;

    @Override
    public void onStart() {
        output = new File("VorkathOutput.txt");
        try {
            writer = new FileWriter("output.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    public int loop() {
        Npc vorkath = Npcs.getNearest("Vorkath");
        System.out.println("Stance: " + vorkath.getStance());
        System.out.println("Animation " + vorkath.getAnimation());


        Log.fine("Stance: " + vorkath.getStance());
        Log.fine("Animation " + vorkath.getAnimation());

        try {
            writer.write("Stance: " + vorkath.getStance());
            writer.write("Animation " + vorkath.getAnimation());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 10;
    }

    @Override
    public void onStop() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }
}
