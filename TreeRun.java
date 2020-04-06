import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Distance;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.movement.transportation.SpiritTree;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

@ScriptMeta(name = "Tree Run",  desc = "Tree Run", developer = "Sri")
public class TreeRun extends Script {

    Player local;
    Position walkToPosition = null;
    private static final Area AREA_VARROCK_TREE = Area.rectangular(new Position(3225, 3455), new Position(3232, 3462));
    private static final Area AREA_LUMBRIDGE_TREE = Area.rectangular(new Position(3190, 3228), new Position(3195, 3234));
    private static final Area AREA_FALADOR_TREE = Area.rectangular(new Position(3001, 3371), new Position(3006, 3376));
    private static final Area AREA_TAVERLEY_TREE = Area.rectangular(new Position(2932, 3433), new Position(2939, 3442));
    private static final Area AREA_LLETYA_TREE = Area.rectangular(new Position(2344, 3159), new Position(2349, 3163));
    private static final Area AREA_GNOME_VILLAGE_TREE = Area.rectangular(new Position(2487, 3177), new Position(2492, 3182));
    private static final Area AREA_ELKOY = Area.rectangular(new Position(2499, 3190), new Position(2503, 3194));
    private static final Area AREA_GNOME_VILLAGE_SPIRIT_TREE = Area.rectangular(new Position(2540, 3167), new Position(2543, 3171));
    private static final Area AREA_GNOME_STRONGHOLD_RIGHT_TREE = Area.rectangular(new Position(2472, 3443), new Position(2478, 3448));
    private static final Area AREA_GNOME_STRONGHOLD_LEFT_TREE = Area.rectangular(new Position(2433, 3412), new Position(2439, 3418));
    private static final Area AREA_BRIMHAVEN_TREE = Area.rectangular(new Position(2762, 3210), new Position(2752, 3200));

    private static final Position POSITION_LUMBRIDGE_TELEPORT = new Position(3219, 3219);
    private static final Position POSITION_FALADOR_TELEPORT = new Position(2964, 3380);
    private static final Position POSITION_TAVERLEY_TELEPORT = new Position(2895, 3455);
    private static final Position POSITION_LLEYTA_TELEPORT = new Position(2334, 3171);
    private static final Position POSITION_MONASTERY_TELEPORT = new Position(2606, 3222);
    private static final Position POSITION_GNOME_VILLAGE_GATE = new Position(2516, 3158);
    private static final Position POSITION_GNOME_STRONGHOLD_SPIRIT_TREE = new Position(2460, 3443);
    private static final Position POSITION_BRIMHAVEN_TELEPORT = new Position(2757, 3175);

    private String state;
    private String navigationState = "toElkoy";
//    private String navigationState = "tpSpiritTree";

    public Position getRandomPosition(List<Position> positionList) {
        Random rand = new Random();
        return positionList.get(rand.nextInt(positionList.size()));
    }

    public Item getInventoryItem(String itemName) {
        Item[] inventoryItems = Inventory.getItems();
        for(Item i: inventoryItems) {
            if (i.getName().contains(itemName)) {
                System.out.println(i.getName());
                return i;
            }
        }
        return null;
    }

    public BooleanSupplier inLocation(Position teleportPosition) {
        BooleanSupplier sup;
        if (Distance.to(teleportPosition) < 15) {
            sup = () -> true;
        }
        else {
            sup = () -> false;
        }
        return sup;
    }

    public BooleanSupplier doesntHaveAction(String action, SceneObject object) {
        BooleanSupplier sup;
        if (hasAction(object.getActions(), action)) {
            sup = () -> false;
        }
        else {
            sup = () -> true;
        }
        return sup;
    }

    public BooleanSupplier dialogIsOpen() {
        BooleanSupplier sup;
        if (Dialog.isOpen()) {
            sup = () -> true;
        }
        else {
            sup = () -> false;
        }
        return sup;
    }

    public BooleanSupplier spiritTreeInterfaceOpen() {
        BooleanSupplier sup;
        if (SpiritTree.isInterfaceOpen()) {
            sup = () -> true;
        }
        else {
            sup = () -> false;
        }
        return sup;
    }

    public BooleanSupplier playerDoingNothing() {
        BooleanSupplier sup;
        if (!local.isMoving() && !local.isAnimating() && !Dialog.isOpen()) {
            sup = () -> true;
        }
        else {
            sup = () -> false;
        }
        return sup;
    }


    public boolean hasAction(String[] actions, String action) {
        for(String s: actions) {
            if (s.equals(action)) {
                return true;
            }
        }
        return false;
    }

    public void walkToTree(String startState, Area area, String endState) {
        if (state.equals(startState)) {
            System.out.println("Walking to: " + startState);
            Movement.walkTo(walkToPosition);
            if (area.contains(local.getPosition())) {
                state = endState;
            }
        }
    }

    public void teleportToSpell(String startState, Spell spell, Area nextArea, String endState, Position teleportPosition) {
        if(state.equals(startState)) {
            System.out.println("Teleporting to: " + startState);
            if (Magic.canCast(spell)) {
                Magic.cast(spell);
                walkToPosition = getRandomPosition(nextArea.getTiles());
                Time.sleepUntil(inLocation(teleportPosition), 2000, 5000);
                state = endState;
            }
            else {
                System.out.println("Unable to teleport to: " + startState);
                state = "stopBot";
            }

        }
    }

    public void teleportToTablet(String startState, String teleportLocation, Area nextArea, String endState, Position teleportPosition) {
        if(state.equals(startState)) {
            System.out.println("Teleporting to: " + startState);
            Item teletab = Inventory.getFirst(teleportLocation + " teleport");
            if (teletab != null) {
                teletab.interact("Break");
                walkToPosition = getRandomPosition(nextArea.getTiles());
                Time.sleepUntil(inLocation(teleportPosition), 2000, 5000);
                state = endState;
            }
            else {
                System.out.println("Unable to teleport to: " + startState);
                state = "stopBot";
            }
        }
    }

    public void teleportToItem(String startState, String itemName, String interaction, Area nextArea, String endState, Position teleportPosition) {
        if(state.equals(startState)) {
            System.out.println("Teleporting to: " + startState);
            Item item = getInventoryItem(itemName);
            if (item != null) {
                item.interact(interaction);
                walkToPosition = getRandomPosition(nextArea.getTiles());
                Time.sleepUntil(inLocation(teleportPosition), 2000, 5000);
                state = endState;
            }
            else {
                System.out.println("Unable to teleport to: " + startState);
                state = "stopBot";
            }
        }
    }

    public void farmTree(String startState, String treeType, String npcName, String patchType, String treeAction, String endState) {
        if (state.equals(startState)) {
            //Check tree if check-health option available
            SceneObject tree = SceneObjects.getNearest(treeType + " tree");
            if (tree != null && hasAction(tree.getActions(), "Check-health")) {
                System.out.println("Checking tree health");
                tree.interact("Check-health");
                Time.sleepUntil(doesntHaveAction("Check-health", tree), 5000);
            }

            //Pay to chop if chop-down option available
            tree = SceneObjects.getNearest(treeType + " tree");
            if (tree != null && hasAction(tree.getActions(), treeAction)) {
                System.out.println("Paying to chop tree");
                Npc npc = Npcs.getNearest(npcName);
                npc.interact("Pay");
                Time.sleepUntil(dialogIsOpen(), 5000);
                if (Dialog.isOpen()) {
                    Dialog.process(0);
                }
                Time.sleepUntil(playerDoingNothing(), 5000);
            }

            SceneObject patch = SceneObjects.getNearest(patchType + " patch");
            if (patch != null && hasAction(patch.getActions(), "Inspect")) {
                System.out.println("Planting tree");
                if (Inventory.getSelectedItem() == null || !Inventory.getSelectedItem().getName().equals(treeType + " sapling")) {

                    Item selectedItem = Inventory.getFirst(treeType + " sapling");
                    if (selectedItem != null) {
                        selectedItem.interact("Use");
                        Time.sleep(1500);
                        patch.interact("Use");
                    }
                    Time.sleepUntil(playerDoingNothing(), 5000);
                }
            }

            SceneObject sapling = SceneObjects.getNearest(treeType + " sapling");
            if (sapling == null) {
                sapling = SceneObjects.getNearest(treeType + " tree");
            }
            if (sapling != null && hasAction(sapling.getActions(), "Inspect")) {
                System.out.println("Paying to watch tree");
                Npc npc = Npcs.getNearest(npcName);
                npc.interact("Pay");
                Time.sleepUntil(dialogIsOpen(), 5000);
                if (Dialog.isOpen()) {
                    Dialog.process(0);
                    state = endState;
                }
                Time.sleepUntil(playerDoingNothing(), 5000);
            }

        }
    }

    public void tpToGnomeStronghold() {
        if (state.equals("navigateToGnomeStronghold")) {
            if (navigationState.equals("toElkoy")) {
                System.out.println("Walking to: " + navigationState);
                Movement.walkTo(new Position(2501, 3191));
                if (AREA_ELKOY.contains(local.getPosition())) {
                    navigationState = "followElkoy";
                }
            }

            if (navigationState.equals("followElkoy")) {
                System.out.println("Following: " + navigationState);
                Npc npc = Npcs.getNearest("Elkoy");
                npc.interact("Follow");
                Time.sleepUntil(inLocation(POSITION_GNOME_VILLAGE_GATE), 5000);
                navigationState = "enterLooseRailing";
            }

            if (navigationState.equals("enterLooseRailing")) {
                System.out.println("Squeeze through: " + navigationState);
                SceneObject gate = SceneObjects.getNearest("Loose Railing");
                gate.interact("Squeeze-through");
                System.out.println("Post squeeze");
                Time.sleepUntil(playerDoingNothing(), 5000);
                System.out.println("Post sleep");
                walkToPosition = getRandomPosition(AREA_GNOME_VILLAGE_SPIRIT_TREE.getTiles());
                navigationState = "toSpiritTree";
            }

            if (navigationState.equals("toSpiritTree")) {
                System.out.println("Walking to: " + navigationState);
                Movement.walkTo(walkToPosition);
                if (AREA_GNOME_VILLAGE_SPIRIT_TREE.contains(local.getPosition())) {
                    navigationState = "tpSpiritTree";
                }
            }

            if (navigationState.equals("tpSpiritTree")) {
                System.out.println("Teleporting to: " + navigationState);
                SpiritTree.open();
                Time.sleepUntil(spiritTreeInterfaceOpen(), 5000);
                if (SpiritTree.isInterfaceOpen()) {
                    SpiritTree.travel(SpiritTree.Destination.GNOME_STRONGHOLD);
                }
                Time.sleepUntil(inLocation(POSITION_GNOME_STRONGHOLD_SPIRIT_TREE), 5000);
                if (inLocation(POSITION_GNOME_STRONGHOLD_SPIRIT_TREE).getAsBoolean()) {
                    state = "walkToGnomeStrongholdRightTree";
                }
            }
        }
    }

    @Override
    public void onStart() {

        Log.fine("This will be executed once on startup.");
        state = "start";
        super.onStart();
    }

    @Override
    public int loop() {
        local = Players.getLocal();
        System.out.println("Loop");

        if (state.equals("start")) {
            walkToPosition = getRandomPosition(AREA_VARROCK_TREE.getTiles());
//            state = "walkToVarrockTree";
            state = "walkToGnomeStrongholdRightTree";

        }

        //Varrock tree
        walkToTree("walkToVarrockTree", AREA_VARROCK_TREE, "farmVarrockTree");
        farmTree("farmVarrockTree", "Yew", "Treznor", "Tree", "Chop down", "tpToLumbridge");

        //Lumbridge tree
        teleportToSpell("tpToLumbridge", Spell.Modern.LUMBRIDGE_TELEPORT, AREA_LUMBRIDGE_TREE, "walkToLumbridgeTree", POSITION_LUMBRIDGE_TELEPORT);
        walkToTree("walkToLumbridgeTree", AREA_LUMBRIDGE_TREE, "farmLumbridgeTree");
        farmTree("farmLumbridgeTree", "Yew", "Fayeth", "Tree", "Chop down", "tpToFalador");

        //Falador tree
        teleportToSpell("tpToFalador", Spell.Modern.FALADOR_TELEPORT, AREA_FALADOR_TREE, "walkToFaladorTree", POSITION_FALADOR_TELEPORT);
        walkToTree("walkToFaladorTree", AREA_FALADOR_TREE, "farmFaladorTree");
        farmTree("farmFaladorTree", "Yew", "Heskel", "Tree", "Chop down", "tpToTaverly");

        //Taverly tree
        teleportToTablet("tpToTaverly", "Taverley", AREA_TAVERLEY_TREE, "walkToTaverlyTree", POSITION_TAVERLEY_TELEPORT);
        walkToTree("walkToTaverlyTree", AREA_TAVERLEY_TREE, "farmTaverleyTree");
        farmTree("farmTaverleyTree", "Yew", "Alain", "Tree", "Chop down", "tpToLletya");

        //Lletya tree
        teleportToItem("tpToLletya", "Teleport crystal", "Lletya", AREA_LLETYA_TREE, "walkToLletyaTree", POSITION_LLEYTA_TELEPORT);
        walkToTree("walkToLletyaTree", AREA_LLETYA_TREE, "farmLletyaTree");
        farmTree("farmLletyaTree", "Palm", "Liliwen", "Fruit Tree", "Pick-coconut", "tpToMonastery");

        //Gnome Village tree
        teleportToItem("tpToMonastery", "Ardougne cloak", "Monastery Teleport", AREA_GNOME_VILLAGE_TREE, "walkToGnomeVillageTree", POSITION_MONASTERY_TELEPORT);
        walkToTree("walkToGnomeVillageTree", AREA_GNOME_VILLAGE_TREE, "farmGnomeVillageTree");
        farmTree("farmGnomeVillageTree", "Palm", "Gileth", "Fruit Tree", "Pick-coconut", "navigateToGnomeStronghold");

        //Gnome Stronghold trees
        tpToGnomeStronghold();
        if (state.equals("walkToGnomeStrongholdRightTree")) {
            walkToPosition = getRandomPosition(AREA_GNOME_STRONGHOLD_RIGHT_TREE.getTiles());
        }
        walkToTree("walkToGnomeStrongholdRightTree", AREA_GNOME_STRONGHOLD_RIGHT_TREE, "farmGnomeStrongholdRightTree");
        farmTree("farmGnomeStrongholdRightTree", "Palm", "Bolongo", "Fruit Tree", "Pick-coconut", "walkToGnomeStrongholdLeftTree");
        if (state.equals("walkToGnomeStrongholdLeftTree")) {
            walkToPosition = getRandomPosition(AREA_GNOME_STRONGHOLD_LEFT_TREE.getTiles());
        }
        walkToTree("walkToGnomeStrongholdLeftTree", AREA_GNOME_STRONGHOLD_LEFT_TREE, "farmGnomeStrongholdLeftTree");
        farmTree("farmGnomeStrongholdLeftTree", "Yew", "Prissy Scilla", "Tree", "Chop down", "tpToBrimhaven");

        //Brimhaven tree
        teleportToTablet("tpToBrimhaven", "Brimhaven", AREA_TAVERLEY_TREE, "walkToBrimhavenTree", POSITION_TAVERLEY_TELEPORT);
        walkToTree("walkToBrimhavenTree", AREA_TAVERLEY_TREE, "farmBrimhavenTree");
        farmTree("farmBrimhavenTree", "Yew", "Garth", "Fruit Tree", "Pick-coconut", "charterToCatherby");


        int delay = (int) (1000 * Math.random());
        return 800 + delay;
    }
}