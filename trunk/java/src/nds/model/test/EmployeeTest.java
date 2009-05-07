/*
 * Created on 2005-2-1
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nds.model.test;
import nds.log.LoggerManager;
import nds.model.*;
import nds.model.dao.*;
import org.hibernate.HibernateException;
import java.util.*;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EmployeeTest {

	public static void main(String[] args) throws HibernateException {
//		 Load the configuration file
		LoggerManager.getInstance().init("/act/conf/nds.properties");
        _RootDAO.initialize();
        GroupuserDAO dao= new GroupuserDAO();
        Groupuser gu=dao.load(new Integer(110251));
        System.out.println(" group name="+gu.getGroup().getName());
        System.out.println(" user name="+gu.getUser().getName());
        Integer i= gu.getUserId();
        System.out.println(" gu.userid="+ i);
        Groupuser gu2=  dao.load(new Integer(110248));
        
        gu.setUser(  gu2.getUser());
        dao.saveOrUpdate( gu);
        
        Integer j= gu2.getUserId();
        System.out.println(" gu2.userid="+ j);
        gu2.setUserId( i);
        dao.saveOrUpdate( gu2);
        
        /*GroupUserDAO dao= new GroupUserDAO();
        GroupUser gu=dao.load(new Integer(110251));
        System.out.println(gu.getGroup().getName());
        System.out.println(gu.getUser().getName());
        */
        /*EmployeeDAO e= new EmployeeDAO();
        Employee emp= e.load(new Integer(111141));
        emp.setCreationdate(new Date());
        e.saveOrUpdate(emp);*/
	}
}
