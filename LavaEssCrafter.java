import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;

@ScriptMeta(developer = "Sri", name = "LavaEssenceCrafter", desc = "Runecrafting")
public class LavaEssCrafter extends Script implements ChatMessageListener {

    private String toTrade;

    @Override
    public int loop() {
        Player player = null;
        if(toTrade != null) {
            player = Players.getNearest(toTrade);
        }
        if(player != null) {
            System.out.println(player.getName() + " wants to to trade me.");
            Time.sleep(4637);
            player.interact("Trade with");
            return -1;
        }
        return 450;
    }


    @Override
    public void notify(ChatMessageEvent chatMessageEvent) {
        if(chatMessageEvent.getMessage().contains("wishes to trade with you")) {
            toTrade = chatMessageEvent.getSource();
        }
    }
}
