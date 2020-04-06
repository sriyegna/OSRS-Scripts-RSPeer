import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;


@ScriptMeta(developer = "Sri", name = "Zammy Wine Grabber", desc = "Telegrabber")
public class ZammyWine extends Script {

    Player local;

    public int getRand() {
        int highRand = Random.high(100, 500);
        int lowRand = Random.low(100, 500);
        int rand;
        if (Random.nextInt(0, 100) < 50) {
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
        super.onStart();
    }

    @Override
    public int loop() {
        local = Players.getLocal();

        Player[] allPlayers = Players.getLoaded();
        for (Player p: allPlayers) {
            if (p.getName() != "sri-1994") {
                Time.sleep(Random.low(150, 500));
                Game.logout();
                return -1;
            }
        }

        if (!Inventory.contains(563)) {
            return -1;
        }

        Pickable zw = Pickables.getNearest(23489);

        int rand = getRand();

        if (Magic.canCast(Spell.Modern.TELEKINETIC_GRAB) && !local.isAnimating() && !local.isMoving() && zw != null) {
            Magic.cast(Spell.Modern.TELEKINETIC_GRAB);
            Time.sleep(rand);
            zw.interact("Cast");
        }

        rand = getRand();

        return 450 + rand;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}