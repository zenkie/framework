package nds.util.test;
import junit.framework.TestCase;
import nds.util.CommandExecuter;


public class CommandExecuterTest extends TestCase {

    public CommandExecuterTest (String name) {
        super(name);
    }
    public void testRun() throws Exception{
        CommandExecuter exec= new CommandExecuter("f:/abc.log");
        String cmd= "ls f:/rt.cmd";
        exec.run(cmd);
    }
}