import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.input.menu.tree.WalkAction;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "Pest Control2", desc = "PC2")
public class PestControl2 extends Script {

    Player local;
    private static final Area BOAT_DOCK = Area.rectangular(new Position(2638, 2655), new Position(2640, 2652));
    private static final Area BOAT_AREA = Area.rectangular(new Position(2632, 2654), new Position(2635, 2649));
    private Area pestControlIslandCenter;

    //12257, 7028
    private Position landingPoint;
    private static final Position BOAT_POSITION = new Position(2638, 2654);

    private String state = "start";
    int gameCount = 0;
    int winCount = 0;

    public int getRand(int low, int high) {
        low = low - 100;
        high = high + 100;
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
    public int loop() {

        local = Players.getLocal();

        if (state.equals("start")) {
//            Log.fine("State is " + state);
            if (BOAT_DOCK.contains(local.getPosition())) {
                Time.sleep(getRand(1000, 2000));
                SceneObjects.getNearest("Gangplank").interact("Cross");
                Time.sleepUntil(() -> BOAT_AREA.contains(Players.getLocal().getPosition()), getRand(4500, 6000));
            }
            if (BOAT_AREA.contains(local.getPosition())) {
                state = "onBoat";
//                Log.fine("State set to " + state);
            }
        }

        if (state.equals("onBoat")) {
//            Log.fine("State is " + state);
//            Time.sleepUntil(() -> !BOAT_AREA.contains(Players.getLocal().getPosition()), 60000);
            if (!BOAT_AREA.contains(local.getPosition()) && local.getX() > 3000) {
                state = "landed";
//                Log.fine("State set to " + state);
            }
        }

        if (state.equals("landed")) {
//            Log.fine("State is " + state);
            Position walkSouth = new Position(Players.getLocal().getPosition().getX() + Random.nextInt(-1, 1), Players.getLocal().getPosition().getY() - Random.nextInt(14, 18));
            Log.fine(walkSouth.toString());
            Time.sleep(getRand(500, 1500));
            Movement.walkTo(walkSouth);
            Time.sleepUntil(() -> Players.getLocal().getPosition().distance(walkSouth) < 5, getRand(25000, 35000));

            Npc voidKnight = Npcs.getNearest("Void Knight"); //
            Position voidKnightBottomLeft = new Position(voidKnight.getPosition().getX() - 3, voidKnight.getPosition().getY() - 3);
            Position voidKnightTopRight = new Position(voidKnight.getPosition().getX() + 3, voidKnight.getPosition().getY() + 3);
            pestControlIslandCenter = Area.rectangular(voidKnightBottomLeft, voidKnightTopRight);
            Movement.walkTo(Random.nextElement(pestControlIslandCenter.getTiles()));
            Time.sleepUntil(() -> pestControlIslandCenter.contains(local.getPosition()), getRand(7500, 13000));

            if (pestControlIslandCenter.contains(local.getPosition())) {
                state = "inCenter";
//                Log.fine("State set to " + state);
            }
        }

        if (state.equals("inCenter")) {
//            Log.fine("State is " + state);
            Npc monster = Npcs.getNearest(n -> (!(n.getName().equals("Void Knight")) && !(n.getName().equals("Squire")) && !(n.getName().equals("")) && pestControlIslandCenter.contains(n.getPosition())));
            if (monster != null) {
                Time.sleep(getRand(600, 1200));
                monster.interact("Attack");
//                Log.fine("Attack " + monster.getName());
                Time.sleepUntil(() -> Players.getLocal().getTarget() == null, 4500);
                //Time.sleepUntil(() -> monster.getHealthPercent() == 0, 15000);
//                Log.fine(monster.getName() + " Killed");
                Time.sleep(getRand(600, 1200));
            }
        }

        if (state.equals("inCenter") && BOAT_DOCK.contains(local.getPosition())) {
//            Log.fine("State is InBoat and " + state);
            state = "start";

            gameCount++;
            Time.sleepUntil(() -> Interfaces.isOpen(231), getRand(3500, 5000));
            if (Interfaces.isOpen(231)) {
                winCount++;
            }
            else if (Interfaces.isOpen(229)) {
                //Loss
            }
            Log.severe("GameCount: " + gameCount);
            Log.severe("WinCount: " + winCount);

//            Log.fine("State set to " + state);
        }

        return getRand(200, 600);
    }
}
