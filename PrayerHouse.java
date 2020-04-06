import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

@ScriptMeta(developer = "Sri", name = "House Run", desc = "Prayer")
public class PrayerHouse extends Script {


    private String houseOwner = "xgrace";
    private int keyWaitMin = 60;
    private int keyWaitMax = 150;
    private int lowDelta = 50;
    private int highDelta = 150;


    private String state = "start";
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

    public void enterName() {
        for(char c: houseOwner.toCharArray()) {
            Keyboard.sendKey(c);
            randomWait(keyWaitMin - lowDelta, keyWaitMax - highDelta);
        }
    }

    @Override
    public int loop() {


        if (state.equals("start")) {
            SceneObject portal = SceneObjects.getNearest(15478);

            if (portal != null) {
                if (!Interfaces.isVisible(162, 40)) {
                    portal.interact("Friend's house");
                }
                Time.sleepUntil(() -> Interfaces.isVisible(162, 40), getRand(3000, 6000));

                InterfaceComponent friendName = Interfaces.getComponent(162, 40);
                if (friendName != null && friendName.isVisible()) {
                    enterName();
                    randomWait(200, 400);
                    Keyboard.pressEnter();
                    Time.sleepUntil(() -> !portalArea.contains(Players.getLocal().getPosition()), getRand(10000, 15000));
                    randomWait(3000, 4500);
                    if (portalArea.contains(Players.getLocal().getPosition())) {
                        Log.severe("Did not enter house. Killed");
                        return -1;
                    }
                }
            }
            if (!portalArea.contains(Players.getLocal().getPosition())) {
                state = "inHouse";
            }
        }

        if (state.equals("inHouse")) {
            SceneObject altar = SceneObjects.getNearest(13197);

            if (altar != null && altar.getPosition().distance(Players.getLocal().getPosition()) > 2) {
                altar.click();
                randomWait(400, 900);
            }

            if (altar != null && altar.getPosition().distance(Players.getLocal().getPosition()) < 3) {
                state = "atAltar";
                Log.fine("Altar not null");
            }
        }

        if (state.equals("atAltar")) {
            //If 2 lit burners
            if (SceneObjects.getLoaded(o -> o.getId() == 13213).length == 2) {
                SceneObject altar = SceneObjects.getNearest(13197);

                Item bones = Inventory.getFirst(536);
                if (bones != null) {
                    bones.interact("Use");
                    Time.sleepUntil(() -> Inventory.isItemSelected(), getRand(2000, 4000));

                    altar.interact("Use");
                    randomWait(800, 1300);
                }
                else {
                    state = "leaveHouse";
                }
            }
        }

        if (state.equals("leaveHouse")) {
            SceneObject portal = SceneObjects.getNearest(4525);
            portal.interact("Enter");
            Time.sleepUntil(() -> portalArea.contains(Players.getLocal().getPosition()), getRand(5000, 10000));
            randomWait(500, 800);

            if (portalArea.contains(Players.getLocal().getPosition())) {
                state = "leftHouse";
            }
        }

        if (state.equals("leftHouse")) {
            if (!phialsArea.contains(Players.getLocal().getPosition())) {
                Movement.walkTo(Random.nextElement(phialsArea.getTiles()));
                Time.sleepUntil(() -> phialsArea.contains(Players.getLocal().getPosition()), getRand(7000, 12000));
                randomWait(400, 900);
            }
            else {
                Npc phials = Npcs.getNearest(1614);

                if (!Inventory.isItemSelected()) {
                    Inventory.getFirst(537).interact("Use");
                    randomWait(400, 900);
                }

                phials.interact("Use");
                Time.sleepUntil(() -> Dialog.isOpen(), getRand(3000, 5000));
                randomWait(300, 700);
                if (Dialog.isOpen()) {
                    Dialog.process(2);
                    Time.sleepUntil(() -> Inventory.contains(536), getRand(2500, 5500));
                    randomWait(350, 750);

                    if (Inventory.contains(536)) {
                        state = "backToPortal";
                    }
                }
            }
        }

        if (state.equals("backToPortal")) {
            Movement.walkTo(Random.nextElement(portalArea.getTiles()));
            Time.sleepUntil(() -> portalArea.contains(Players.getLocal().getPosition()), getRand(7000, 13000));
            randomWait(600, 1000);

            if (portalArea.contains(Players.getLocal().getPosition())) {
                state = "start";
            }
        }


        return 0;
    }
    //If state = outsidePortal
        //Interact house portal Friends
        //Enter name

    //If state = insideHouse
        //Walk to Gilded Altar

    //If at altar
        //If bones in inventory
            //Check if both burners lit
            //Pray bones
        //If no bones in inventory
            //Walk to portal
            //Exit house

    //If leftHouse
        //Go to general store
        //Exchange piles
        //Walk to portal
}
