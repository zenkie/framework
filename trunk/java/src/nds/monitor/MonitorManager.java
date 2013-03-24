package nds.monitor;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import nds.control.web.ServletContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.io.PluginController;
import nds.io.PluginManager;
import nds.io.PluginScanner;
import nds.io.scanner.DeploymentEvent;
import nds.io.scanner.DeploymentListener;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.CollectionValueHashtable;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.Tools;
import org.apache.velocity.app.Velocity;

public class MonitorManager
{
	private static Logger logger = LoggerManager.getInstance().getLogger(MonitorManager.class.getName());
	private static MonitorManager instance = null;
	private boolean isInitialized = false;
	private PluginManager<ObjectActionListener> oaLmanager;
	private boolean isMonitorPluginInstalled = false;
	private CollectionValueHashtable objActionListeners;
	private Hashtable<Integer, ObjectActionListener> objActionListenersIds;

	private MonitorManager()
	{
		this.objActionListeners = new CollectionValueHashtable();
		this.objActionListenersIds = new Hashtable();
	}

	public String getMonitorRootFolder(String paramString)
	{
		return WebUtils.getProperty("export.root.nds", "/aic/home") + File.separator + paramString + File.separator + "monitor";
	}

	public void checkMonitorPlugin()
	{
		//System.out.print("asdfasdfasd");
		if (!this.isMonitorPluginInstalled) {
			logger.debug("not install");
			throw new NDSRuntimeException("@monitor-not-installed@");
		}
		logger.debug("installed");
	}

	public boolean isMonitorPluginInstalled()
	{
		return this.isMonitorPluginInstalled;
	}

	public void dispatchEvent(ObjectActionEvent paramObjectActionEvent) {
		if (!this.isMonitorPluginInstalled)
			return;
		try {
			logger.debug("dispatch "+paramObjectActionEvent.getClass().getName());
			Collection b = this.objActionListeners.get(paramObjectActionEvent
					.getAdClientId()
					+ "."
					+ paramObjectActionEvent.getTableId());
			if (b != null)
				for(Iterator it=b.iterator();it.hasNext();){
					ObjectActionListener localObjectActionListener;
					localObjectActionListener = (ObjectActionListener)it.next();
					localObjectActionListener.onAction(paramObjectActionEvent);
				}
			return;
		} catch (Exception e) {
			logger.error("Error dispatch " + paramObjectActionEvent.toString(),
					e);

			throw new NDSRuntimeException("@error-dispatch-monitor-event@:"
					+ e.getMessage());
		}
	}

	private ObjectActionListener createListener(int paramInt) throws Exception
	{
		ObjectActionListener v=(ObjectActionListener)this.oaLmanager.findPlugin("default");
		if (v != null) {
			try
			{
				ObjectActionListener localObjectActionListener=v.getClass().newInstance();
				logger.debug("monid :"+String.valueOf(paramInt));
				localObjectActionListener.init(paramInt);
				localObjectActionListener.setDebug(true);
				return localObjectActionListener;
			} catch (Throwable e) {
				logger.error("fail to new ObjectActionListener of " + e.getClass(), e);
			}
		}
		return null;
	}

	public synchronized void reloadListener(int paramInt) throws Exception {
		
		removeListener(paramInt);
		
		ObjectActionListener localObjectActionListener;
		localObjectActionListener = createListener(paramInt);
		
		if (localObjectActionListener == null) {
			throw new NDSException(
					"No implementation for ObjectActionListener, check your plugin configuration");
		}
		this.objActionListeners.add(localObjectActionListener.getAdClientId()
				+ "." + localObjectActionListener.getTableId(),
				localObjectActionListener);
		this.objActionListenersIds.put(Integer.valueOf(paramInt),
				localObjectActionListener);
	}
	
	

	public synchronized ObjectActionListener removeListener(int paramInt)
			throws NDSException {
		Object localObject;
		localObject = (ObjectActionListener) this.objActionListenersIds
				.get(Integer.valueOf(paramInt));
		if (localObject != null) {

			Collection b = this.objActionListeners
					.get(((ObjectActionListener) localObject).getAdClientId()
							+ "."
							+ ((ObjectActionListener) localObject).getTableId());

			for (Iterator it = b.iterator(); it.hasNext();) {
				ObjectActionListener localObjectActionListener;
				if ((localObjectActionListener = (ObjectActionListener) it
						.next()).getId() == paramInt) {
					b.remove(localObjectActionListener);
					break;
				}
			}
			return (ObjectActionListener) this.objActionListenersIds
					.remove(Integer.valueOf(paramInt));
		}
		return null;
	}
	
	
	

	public synchronized void reloadObjectActionListeners() throws Exception {
		this.objActionListeners.clear();
		List localList;
		try {
			localList = QueryEngine
					.getInstance()
					.doQueryList(
							"select id from ad_monitor m where m.check_type='action' and m.monitor_type='obj' and m.isactive='Y'");
		} catch (Throwable e) {
			throw new NDSException("Fail to read ad_monitor table from db", e);
		}
		if (localList != null)
			for (int i = 0; i < localList.size(); i++) {
				int j = Tools.getInt(localList.get(i), -1);
				ObjectActionListener localObjectActionListener = createListener(j);
				logger.debug("reloadObjectActionListeners complet!");
				if (localObjectActionListener == null) {
					throw new NDSException(
							"No implementation for ObjectActionListener, check your plugin configuration");
				}
				this.objActionListeners.add(
						localObjectActionListener.getAdClientId() + "."
								+ localObjectActionListener.getTableId(),
						localObjectActionListener);
				this.objActionListenersIds.put(Integer.valueOf(j),
						localObjectActionListener);
			}
	}

	public List getModifiableMonitors(Table paramTable,
			UserWebImpl usr ) throws Exception {
		//this = (this = TableManager.getInstance()).getTable("ad_monitor");
		Table mo_table=TableManager.getInstance().getTable("ad_monitor");
		QueryRequestImpl query;
		query = QueryEngine.getInstance().createRequest(usr.getSession());
		query.setMainTable(mo_table.getId());
		query.addSelection(mo_table.getColumn("id").getId());
		query.addSelection(mo_table.getColumn("name").getId());
		query.addSelection(mo_table.getColumn("monitor_type").getId());
		query.addSelection(mo_table.getColumn("check_type").getId());
		query.addSelection(mo_table.getColumn("isactive").getId());

		query.addOrderBy(
				new int[] { mo_table.getColumn("name").getId() }, true);

		Expression localExpression = (localExpression = (localExpression = (localExpression = new Expression(
				new ColumnLink("ad_monitor.monitor_type"), "=obj", null))
				.combine(new Expression(new ColumnLink(
						"ad_monitor.monitor_type"), "=list", null), 2, null))
				.combine(usr.getSecurityFilter(mo_table.getName(), 3), 1,
						null)).combine(new Expression(new ColumnLink(
				"ad_monitor.ad_table_id"), "=" + paramTable.getId(), null), 1,
				null);
		query.addParam(localExpression);
		logger.debug("getModifiableMonitors sql is:"+query.toSQL());
		return QueryEngine.getInstance().doQueryList(query.toSQL());
	}

	public static synchronized MonitorManager getInstance() {
		if (instance == null) {
			instance = new MonitorManager();
		}
		if (!instance.isInitialized) {
			try {
				MonitorManager localMonitorManager = instance;
				PluginController localPluginController = (PluginController) WebUtils
						.getServletContextManager().getActor(
								"nds.io.plugincontroller");
				localMonitorManager.oaLmanager = new PluginManager(
						ObjectActionListener.class,
						localPluginController.getPluginScanner());
				localMonitorManager.oaLmanager.init();
				if (localMonitorManager.oaLmanager.findPlugin("default") != null) {
					localPluginController.getPluginScanner()
							.addDeploymentListener(new a());
					localMonitorManager.reloadObjectActionListeners();
					logger.info("Monitor plugin found.");
					localMonitorManager.isMonitorPluginInstalled = true;
				} else {
					localMonitorManager.isMonitorPluginInstalled = false;
					logger.warning("Monitor plugin not installed.");
				}
				localMonitorManager.isInitialized = true;
				Velocity.init();
			} catch (Exception localException) {
				throw new NDSRuntimeException(localException.getMessage(),
						localException);
			}
		}
		return instance;
	}

	
	static  class a implements DeploymentListener {
		a() {
		}

		public final void urlDeployed(DeploymentEvent paramDeploymentEvent) {
			try {
				MonitorManager.getInstance().reloadObjectActionListeners();
				return;
			} catch (Throwable e) {
				logger.error("fail to reload listeners", e);
			}
		}

		public final void urlModified(DeploymentEvent paramDeploymentEvent) {
			try {
				MonitorManager.getInstance().reloadObjectActionListeners();
				return;
			} catch (Throwable e) {
				logger.error("fail to reload listeners", e);
			}
		}

		public final void urlRemoved(DeploymentEvent paramDeploymentEvent) {
			try {
				MonitorManager.getInstance().reloadObjectActionListeners();
				return;
			} catch (Throwable e) {
				logger.error("fail to reload listeners", e);
			}
		}
	}

}

