package nds.control.ejb;
import java.sql.Connection;
public interface Trigger {
    /**
     * @return return msg, can be anything
     */
    public String execute(int objectId, Connection conn);
}