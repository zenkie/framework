package nds.portlet.action.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.portlet.PortletConfig;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nds.control.event.DefaultWebEvent;
import nds.control.util.MenuItem;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.portlet.action.ActionHandler;
import nds.portlet.util.PortletUtils;
import nds.util.WebKeys;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/*
Load menu according to RenderRequest parameter "menu_root", if not found,
Load from PortletPreferences "menu_root"
*/
public class Menu extends ActionHandler{
    public Menu() {
    }

    public String handle(ActionMapping mapping, ActionForm form,
                         PortletConfig config, RenderRequest req,
                         RenderResponse res,HashMap paramMap) throws Exception {
            req.setAttribute("nds.portlet.menu",
                             constructMenu_test("Root", req));
            //req.setAttribute("nds.portlet.chain.menu", constructMenu(rootMenu, req));
        String fwd ="portlet.mainmenu.view";
        return fwd;
    }

    private nds.control.util.Menu constructMenu_test(String root, RenderRequest req) throws
        Exception {
        nds.control.util.Menu menu = new nds.control.util.Menu(1, "menu", true, true, "menu1", new Date(),
                             new Date(), 0);

        MenuItem item1 = new MenuItem(1, "item1", "icon1", "menu_root=item1", 1,
                                      "menu", 1, true);
        MenuItem item2 = new MenuItem(2, "item2", "icon2", "table=Employee", 2,
                                      "objectlist", 2, false);
        menu.addRootItem(item1);
        menu.addRootItem(item2);
        item2 = new MenuItem(3, "item1.1", "icon2", "menu_root=item1.1", 3,
                             "menu", 3, true);
        item1.addChild(item2);
        item1.addChild(new MenuItem(4, "item1.2", "icon2", "menu_root=item1.2",
                                    4, "menu", 4, true));
        return menu;
    }

    /**
     * Construct menu according to menu root node specified
     * @param root String
     * @param req from which user information will be fetched
     * @return NavNode, null if specified menu root not found
     */
    private Menu constructMenu(String root, RenderRequest req) throws Exception {

        HttpServletRequest hq = PortletUtils.getHttpServletRequest(req);
        HttpSession session = hq.getSession(true);
        SessionContextManager scm = WebUtils.getSessionContextManager(session);
        if (scm == null)return null;
        UserWebImpl user = (UserWebImpl) scm.getActor(nds.util.WebKeys.USER);
        if (user.isLoggedIn()) {
            return null;
        }
        DefaultWebEvent event = new DefaultWebEvent("CommandEvent");
        event.setParameter("command", "GetNavigateMenu");
        event.setParameter("operatorid", user.getUserId() + "");
        event.setParameter("menuName", root);
            if(user !=null && user.getSession()!=null)
            	event.put("nds.query.querysession",user.getSession());

        ClientControllerWebImpl controller = (ClientControllerWebImpl) WebUtils.
            getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);

        // holder contains tree list
        ValueHolder holder = null;
        Vector nodes = null;
        holder = controller.handleEvent(event);
        return (Menu) holder.get("menu");

    }
}
