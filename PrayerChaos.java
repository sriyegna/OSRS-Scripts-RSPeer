import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Chaos Run", desc = "Prayer")
public class PrayerChaos extends Script {


    private int keyWaitMin = 60;
    private int keyWaitMax = 150;
    private int lowDelta = 50;
    private int highDelta = 150;


    private String state = "start";
    private String nextState;
    private int combatLevel;
    private int wildernessLevel;

    private Area portalArea = Area.rectangular(new Position(2953, 3221), new Position(2955, 3226));
    private Area phialsArea = Area.rectangular(new Position(2947, 3212), new Position(2950, 3215));

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

    public boolean checkPlayers(Player[] players) {
        for(Player p: players) {
            Log.fine(p.getName() + " " + p.getCombatLevel());
            if (p.getCombatLevel() > combatLevel || p.getCombatLevel() >= (combatLevel - wildernessLevel)) {
                Game.logout();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        combatLevel = Players.getLocal().getCombatLevel();
        wildernessLevel = 40;
        super.onStart();
    }

    @Override
    public int loop() {

        if (combatLevel > 100) {
            //Check if we need to logout
            if (Players.getLoaded().length > 1) {
                if (checkPlayers(Players.getLoaded())) {
                    Game.logout();
                    return -1;
                }
            }


            if (state.equals("atAltar")) {
                SceneObject altar = SceneObjects.getNearest(411);
                Item bones = Inventory.getFirst(536);
                if (bones != null && altar != null) {
                    bones.interact("Use");
                    Time.sleepUntil(() ->checkPlayers(Players.getLoaded()) || Inventory.isItemSelected(), getRand(2000, 4000));

                    altar.interact("Use");
                    randomWait(800, 1300);
                }
                else {
                    state = "checkDoors";
                    nextState = "talkToDruid";
                }
            }

            if (state.equals("checkDoors")) {
                if (SceneObjects.getNearest(1521) != null && SceneObjects.getNearest(1524) != null) {
                    //Open door
                    if (getRand(0, 100) < 50) {
                        SceneObjects.getNearest(1521).interact("Open");
                    }
                    else {
                        SceneObjects.getNearest(1524).interact("Open");
                    }
                    Time.sleepUntil(() -> checkPlayers(Players.getLoaded()) || SceneObjects.getNearest(1522) != null || SceneObjects.getNearest(1525) != null, getRand(5000, 8000));
                    randomWait(600, 1100);
                }

                if (SceneObjects.getNearest(1522) != null || SceneObjects.getNearest(1525) != null) {
                    state = nextState;
                }
            }

            if (state.equals("talkToDruid")) {
                Npc druid = Npcs.getNearest(7995);

                randomWait(400, 1100);
                if (!Inventory.isItemSelected()) {
                    Inventory.getFirst(537).interact("Use");
                    randomWait(400, 900);
                }

                druid.interact("Use");
                Time.sleepUntil(() -> checkPlayers(Players.getLoaded()) || Dialog.isOpen(), getRand(3000, 5000));
                randomWait(300, 700);
                if (Dialog.isOpen()) {
                    //Dialog.process(2);
                    Time.sleepUntil(() -> checkPlayers(Players.getLoaded()) || Inventory.contains(536), getRand(2500, 5500));
                    randomWait(350, 750);

                    if (Inventory.contains(536)) {
                        state = "checkDoors";
                        nextState = "backToAltar";
                    }
                }
            }

            if (state.equals("backToAltar")) {
                SceneObject altar = SceneObjects.getNearest(411);

                if (altar != null && altar.getPosition().distance(Players.getLocal().getPosition()) > 2) {
                    altar.click();
                    randomWait(400, 900);
                }

                if (altar != null && altar.getPosition().distance(Players.getLocal().getPosition()) < 3) {
                    state = "start";
                }
            }


        }

        return 0;
    }
}
