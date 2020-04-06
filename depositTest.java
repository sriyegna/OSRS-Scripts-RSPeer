import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.DepositBox;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;

@ScriptMeta(developer = "Name", name = "DepositTest", desc = "Test")
public class depositTest extends Script {

    @Override
    public int loop() {
        DepositBox.open();
        Time.sleepUntil(() -> DepositBox.isOpen(), 10000);
        return 0;
    }
}
