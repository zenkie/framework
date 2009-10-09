package nds.control.ejb;
import java.sql.Connection;
import nds.query.SPResult;
public interface Trigger {
    /**
     * @return return SPResult, with code and message
			其中 p_code:
			代码描述（与单对象界面按钮/菜单项的处理方式一致）
			0 不刷新
			1 刷新当前对象页
			2 关闭当前对象窗口
			3 尝试刷新当前界面的明细标签页，如果失败（如：不存在明细标签页），刷新当前页面
			4 以p_message内容作为新的URL，按URL目标页定义替换当前页面的DIV或者构造HREF
			5 以p_message内容作为新的JAVASCRIPT, 解析并执行
			99 关闭当前窗口
     */
    public SPResult execute(int objectId, Connection conn);
}