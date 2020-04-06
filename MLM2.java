import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.DepositBox;
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


@ScriptMeta(developer = "Sri", name = "Motherlode2", desc = "Mining")
public class MLM2 extends Script {

    private String state = "start";
//    private String state = "depositToBank";

    private Area firstFloorLadder = Area.rectangular(new Position(3756, 5671), new Position(3753, 5673));
    private Area secondFloorLadder = Area.rectangular(new Position(3753, 5677), new Position(3757, 5674));
    private Area bankArea = Area.rectangular(new Position(3760, 5669), new Position(3758, 5665));
    private Area oreVeinArea = Area.rectangular(new Position(3748, 5676), new Position(3762, 5684));
    private Position secondFloorPosition = new Position(3755, 5675);
    private Position firstFloorPosition = new Position(3755, 5672);
    private Position rockfallOne = new Position(3757, 5677);
    private Position rockfallTwo = new Position(3748, 5684);

    private int lowDelta = 80;
    private int highDelta = 170;
    private int depositCount = 0;

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


    @Override
    public int loop() {

        //Mine rockfall if we are within 1 block of it
        if (!Players.getLocal().isAnimating()) {
            SceneObject rockfall = SceneObjects.getNearest("Rockfall");
            if (rockfall != null && rockfall.getPosition().distance(Players.getLocal().getPosition()) == 1) {
                rockfall.interact("Mine");
                Time.sleepUntil(() -> !Players.getLocal().isAnimating(), getRand(5000, 10000));
                Time.sleep(getRand(500, 1100));
            }
        }

        if (state.equals("start")) {
            //Walk to ladder if not moving
            if (!Players.getLocal().isMoving() && Players.getLocal().getTarget() == null) {
                Movement.walkTo(Random.nextElement(firstFloorLadder.getTiles()));
                randomWait(400, 600);
            }

            //If ladder is within 5, 7 tiles, click it
            SceneObject ladder = SceneObjects.getNearest("Ladder");
            int dist = Random.nextInt(5, 7);
            if (ladder != null && ladder.getPosition().distance(Players.getLocal().getPosition()) < dist) {
                ladder.interact("Climb");
                Time.sleepUntil(() -> secondFloorPosition.distance(Players.getLocal().getPosition()) == 0, getRand(7000, 15000));
                randomWait(450, 850);
            }

            //If on second floor, change state
            if (secondFloorPosition.distance(Players.getLocal().getPosition()) == 0) {
                state = "mining";
            }
        }

        if (state.equals("mining")) {

            Item gem = Inventory.getFirst(g -> g.getName().equals("Uncut sapphire") || g.getName().equals("Uncut emerald") || g.getName().equals("Uncut ruby") || g.getName().equals("Uncut diamond"));
            if (gem != null) {
                gem.interact("Drop");
                randomWait(800, 1400);
            }

            //Mine ore vein
//            SceneObject oreVein = SceneObjects.getNearest("Ore vein");
            SceneObject oreVein = SceneObjects.getNearest(ov -> ov.getName().equals("Ore vein") && oreVeinArea.contains(ov.getPosition()));
//            SceneObject oreVein = SceneObjects.getNearest(ov -> ov.getName() == "Ore vein" && ov.getPosition().distance(rockfallOne) != 2 && ov.getPosition().distance(rockfallTwo) != 2);
            if (oreVein != null) {
                if (!Players.getLocal().isAnimating()) {
                    oreVein.interact("Mine");
                    Log.fine("Mining");
                }
                Time.sleepUntil(() -> !Players.getLocal().isAnimating() && Players.getLocal().getTarget() == null, getRand(29000, 35000));
                randomWait(700, 1400);
            }

            if (Inventory.isFull()) {
                state = "climbDown";
            }
        }

        if (state.equals("climbDown")) {
            //If user is not moving, walk to second floor ladder
            if (!Players.getLocal().isMoving()  && Players.getLocal().getTarget() == null) {
                Movement.walkTo(Random.nextElement(secondFloorLadder.getTiles()));
                randomWait(500, 800);
            }

            //If ladder is within 3, 6 tiles, click it
            SceneObject ladder = SceneObjects.getNearest("Ladder");
            int dist = Random.nextInt(3, 6);
            if (ladder != null && ladder.getPosition().distance(Players.getLocal().getPosition()) < dist) {
                ladder.interact("Climb");
                Time.sleepUntil(() -> firstFloorPosition.distance(Players.getLocal().getPosition()) == 0, getRand(7000, 15000));
                randomWait(450, 850);
            }

            //If on first floor coordinate
            if (firstFloorPosition.distance(Players.getLocal().getPosition()) == 0) {
                state = "depositHopper";
            }
        }

        if (state.equals("depositHopper")) {
            //Hopper - Deposit

            SceneObject hopper = SceneObjects.getNearest("Hopper");
            if (hopper != null && Inventory.isFull()) {
                hopper.interact("Deposit");
                Time.sleepUntil(() -> Inventory.isEmpty(), getRand(10000, 15000));
                randomWait(700, 1800);

                if (Inventory.isEmpty()) {
                    depositCount++;
                    if (depositCount == 3) {
                        state = "depositToBank";
                    }
                    else {
                        state = "start";
                    }
                }
            }
        }

        if (state.equals("depositToBank")) {
            Log.fine("Deposit");
            SceneObject sack = SceneObjects.getNearest("Sack");

            //Check within this function
            if (sack != null && Inventory.isEmpty()) {
                sack.interact("Search");
                Log.info("Before search sleep");
                Time.sleepUntil(() -> !Inventory.isEmpty(), getRand(8000, 13000));
                Log.info("After search sleep");
                randomWait(2000, 3000);

                Log.info("Before move");
                if (!Inventory.isEmpty() && !Players.getLocal().isMoving()) {
                    Movement.walkTo(Random.nextElement(bankArea.getTiles()));
                    randomWait(300, 700);
                }
                Log.info("After move");
            }

            //If depositBox is within 3, 6 tiles, click it
            SceneObject bankChest = SceneObjects.getNearest(26707);
            int dist = Random.nextInt(5, 8);
            if (!Inventory.isEmpty() && bankChest != null && bankChest.getPosition().distance(Players.getLocal().getPosition()) <= dist) {
                Log.fine("In dp if");
//                Bank.open();
                bankChest.interact("Use");
                Time.sleepUntil(() -> Bank.isOpen(), getRand(7000, 15000));
                randomWait(550, 950);

                Bank.depositInventory();
                Time.sleepUntil(() -> Inventory.isEmpty(), getRand(5000, 10000));
                randomWait(400, 900);

                Bank.close();
                Log.fine("Before close sleep");
                Time.sleepUntil(() -> !Bank.isOpen(), getRand(6000, 12000));
                Log.fine("After close sleep");
                randomWait(550, 950);

                depositCount--;
            }

            //If deposited all, go to start
            if (depositCount == 0 && Inventory.isEmpty() && !Bank.isOpen()) {
                Log.fine("State switch");
                state = "start";
            }
        }

        return 0;
    }
}
