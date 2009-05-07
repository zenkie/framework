package nds.saasifc.alisoft;

public interface OrderConstants {
//		public static final String PARAMETER_APPKEY = "sip_appkey";
		public static final String PARAMETER_SIGNATURE="signature";//签名
		public static final String PARAMETER_SUBSCTYPE="subscType" ;//订购类型
		public static final String PARAMETER_APPID="appId" ;//所订购的软件id
		public static final String PARAMETER_APPEND="appEnd" ;//软件服务的截止时间 
		public static final String PARAMETER_GMTSTART="gmtStart";//订单开始时间 
		public static final String PARAMETER_SUBSCEND="subscEnd" ;//订购控制记录的结束时间
		public static final String PARAMETER_CTRLPARAMS="ctrlParams" ;//控制参数
		public static final String PARAMETER_RETURNURL="returnUrl" ;//订购页面参数回传地址
		public static final String PARAMETER_POSTDATA="postData" ;//订购页面要原样回传的参数
		public static final String PARAMETER_SIGN="sign";
		public static final String PARAMETER_APPINSTANCEID="appInstanceId";//应用实例ID
		public static final String PARAMETER_EVENT="event";//**类型,新订、续订、资源订购及退订
		public static final String PARAMETER_USERID="userId";//用户ID
		public static final String PARAMETER_SUBSCID="subscId";//订单ID
		public static final String PARAMETER_GMTEND="gmtEnd";//订单结束时间
		public static final String PARAMETER_TOTALAMOUNT="totalAmount";//订单总金额
		public static final String PARAMETER_AMOUNT="amount";//实付金额
		public static final String PARAMETER_RENTAMOUNT="rentAmount";//月租额
		public static final String PARAMETER_RESOURCEAMOUNT="resourceAmount";//购买资源金额
		public static final String PARAMETER_COUPONAMOUTN="couponAmount";//红包
}
