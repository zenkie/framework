/******************************************************************
*
*$RCSfile: DBNames.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: DBNames.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.2  2002/12/17 08:45:38  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.11  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.10  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.9  2001/11/29 13:13:14  yfzhu
*no message
*
*Revision 1.8  2001/11/29 00:48:49  yfzhu
*no message
*
*Revision 1.7  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.6  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.5  2001/11/13 22:47:52  song
*add sheet_status ... static fields
*
*Revision 1.4  2001/11/10 04:12:33  yfzhu
*no message
*
*Revision 1.3  2001/11/08 15:10:51  yfzhu
*First time compile OK
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.util;

/**
 * Contains table names, these will be used for sequence generation
 */
public interface DBNames {
    // for customerRec table
    public final static String CUSTRECSAVAMTADJSHT = "custrecsavamtadjsht";
    public final static String CUSTRECQTYERRSHT = "custrecqtyerrsht";
    public final static String CUSTRECQTYERRSHTITEM = "custrecqtyerrshtitem";
    public final static String CUSTRECQTYERRADJSHT = "custrecqtyerradjsht";
    public final static String CUSTRECQTYERRADJSHTITEM = "custrecqtyerradjshtitem";
    public final static String CUSTRECCONFIRMSHT = "custrecconfirmsht";
    public final static String CUSTRECCONFIRMSHTITEM = "custrecconfirmshtitem";
    public final static String CUSTRECSAVAMTADJSHTITEM = "custrecsavamtadjshtitem";

    //for income table
    public final static  String CUSTINCOMESHT = "custincomesht";


    // for basicinfo table
    public final static String RULE = "rule";
    public final static String USER = "user";
    public final static String GROUP = "group";
    public final static String GROUPPERM = "groupperm";
    public final static String DEPARTMENT ="department";
    public final static String EMPLOYEE = "employee";
    public final static String STOCK = "stock";
    public final static String CUSTOMER = "customer";
    public final static String CUSTOMERSORT = "customersort";
    public final static String DELIVERER = "deliverer";
    public final static String PFTBUYERSORT = "pftbuyersort";
    public final static String PRODUCT = "product";
    public final static String PRODUCTSORT = "productsort";
    public final static String VENDOR = "vendor";
    public final static String WAREHOUSE = "warehouse";
    public final static String DEPTUSABLEQUOTA = "deptusablequota";
    public final static String OUTLETSTORAGE = "outletstorage";
    public final static String STOCKSTORAGE = "stockstorage";
    public final static String WAREHOUSESTORAGE = "warehousestorage";
    public final static String BGTDEPTQTYACCUM = "bgtdeptqtyaccum";


   // for distribution table

    public final static  String DISREQUESTSHT = "disrequestsht";
    public final static  String DISREQUESTSHTITEM = "disrequestshtitem";
    public final static  String DISSHIPSHT = "disshipsht";
    public final static  String DISSHIPSHTITEM = "disshipshtitem";
    public final static  String DISSHT = "dissht";
    public final static  String DISSHTITEM = "disshtitem";
    public final static  String DISSHIPTRANSSHT = "disshiptranssht";
    public final static  String DISSHIPTRANSSHTITEM = "disshiptransshtitem";

    // for ftp table
    public final static String CHEQTYERRADJFTP = "cheqtyerradjftp";
    public final static String CUSSAVAMTADJFTP = "cussavamtadjftp";
    public final static String CUSTRECCONFIRMFTP = "custrecconfirmftp";
    public final static String CUSTRECQTYERRADJFTP = "custrecqtyerradjftp";
    public final static String DEALERSALEFTP = "dealersaleftp";
    public final static String FREESALEFTP = "freesaleftp";
    public final static String ORDERLANDFTP = "orderlandftp";
    public final static String OUTLETDAYSALEFTP = "outletdaysaleftp";
    public final static String OUTLETRECFTP = "outletrecftp";
    public final static String OUTLETSHIPFTP = "outletshipftp";
    public final static String PROMOTIONSALEFTP = "promotionsaleftp";
    public final static String RETURNQTYERRADJFTP = "returnqtyerradjftp";
    public final static String STOPDTCOSTADJFTP = "stopdtcostadjftp";

   // for check table
    public final static  String CHEQTYERRADJSHT = "cheqtyerradjsht";
    public final static  String CHEQTYERRADJSHTITEM = "cheqtyerradjshtitem";
    public final static  String OUTLETCHEQTYERRSHT = "outletcheqtyerrsht";
    public final static  String OUTLETCHEQTYERRSHTITEM = "outletcheqtyerrshtitem";
    public final static  String WHCHEQTYERRSHT = "whcheqtyerrsht";
    public final static  String WHCHEQTYERRSHTITEM = "whcheqtyerrshtitem";

    // wasteCancel table
    public final static  String WASTECANCELSHT = "wastecancelsht";
    public final static  String WASTECANCELSHTITEM = "wastecancelshtitem";
    public final static  String WASTECANCELTRANSSHT = "wastecanceltranssht";
    public final static  String WASTECANCELTRANSSHTITEM = "wastecanceltransshtitem";

    // preferential table
    public final static  String PREFERENTIALSHT = "preferentialsht";
    public final static  String PREFERENTIALSHTITEM = "preferentialshtitem";
    // returnland table
    public final static  String RETURNLANDSHT = "returnlandsht";
    public final static  String RETURNLANDSHTITEM = "returnlandshtitem";
    public final static  String RETURNQTYERRADJSHT = "returnqtyerradjsht";
    public final static  String RETURNQTYERRADJSHTITEM = "returnqtyerradjshtitem";
    public final static  String RETURNRECSHT = "returnrecsht";
    public final static  String RETURNRECSHTITEM = "returnrecshtitem";
    public final static  String RETURNSAVAMTADJSHT = "returnsavamtadjsht";
    public final static  String RETURNSAVAMTADJSHTITEM = "returnsavamtadjshtitem";
    public final static  String RETURNLANDTRANSSHT = "returnlandtranssht";
    public final static  String RETURNLANDTRANSSHTITEM = "returnlandtransshtitem";

    // stocktransfer table
    public final static  String STOCKTRANSFERSHT = "stocktransfersht";
    public final static  String STOCKTRANSFERSHTITEM = "stocktransfershtitem";
    // outlettransfer table
    public final static  String TRANSFERSHT = "transfersht";
    public final static  String TRANSFERSHTITEM = "transfershtitem";
    // costupadj table
    public final static String PDTCOSTUPADJSHT = "pdtcostupadjsht";
    public final static String PDTCOSTUPADJSHTITEM = "pdtcostupadjshtitem";

    // orderland table
    public final static String  DEPTBGTITEM = "deptbgtitem";
//    public final static String  ORDERBGT_LANDQTYDIFF = "orderbgt_landqtydiff";
    public final static String  ORDERBGTSHT     = "orderbgtsht";
    public final static String  ORDERBGTSHTITEM = "orderbgtshtitem";
    public final static String  ORDER_LANDQTYDIFF = "order_landqtydiff";
    public final static String  ORDERLANDSHT    = "orderlandsht";
    public final static String  ORDERLANDSHTITEM= "orderlandshtitem";
    public final static String  ORDERRECSHT     = "orderrecsht";
    public final static String  ORDERRECSHTITEM = "orderrecshtitem";
    public final static String  USABLEQUOTATRANSSHT     = "usablequotatranssht";
    public final static String  USABLEQUOTATRANSSHTITEM = "usablequotatransshtitem";
    public final static String  BGTLANDQTYDIFFSHT     = "bgt_landqtydiffsht";
    public final static String  BGTLANDQTYDIFFSHTITEM = "bgt_landqtydiffshtitem";
    public final static String  ORDERLANDTRANSSHT     = "orderlandtranssht";
    public final static String  ORDERLANDTRANSSHTITEM     = "orderlandtransshtitem";



    // monthcloseoff table
    public final static String MMCLOSEOFFDATESHT = "mmcloseoffdatesht";
    public final static String MMCLOSEOFFDATESHTITEM = "mmcloseoffdateshtitem";
    public final static String MMCLOSEOFFMMFTP = "mmcloseoffmmftp";
    public final static String OLTMMCOFBILLAMTSHT = "oltmmcofbillamtsht";
    public final static String OLTMMCOFBILLAMTSHTITEM = "oltmmcofbillamtshtitem";
    public final static String CUSTMMBALANCEFM = "custmmbalancefm";
    public final static String CUSTMMBALANCEFMITEM = "custmmbalancefmitem";
    // outletdaysale table
    public final static String OUTLETDAYSALESHT = "outletdaysalesht";
    public final static String OUTLETDAYSALESHTITEM = "outletdaysaleshtitem";
    // datecloseoff table
    public final static String DDCOFDATEFTP = "ddcofdateftp";
    // pos table
    public final static String POSEMPLOYEE = "posemployee";
    public final static String POSDISCRIGHT = "posdiscright";
    // The next is for commom variable define

    public final static int TRUE = 1;  // 正确
    public final static int FALSE = 2; // 错误


    public final static int RETURNRECSHT_RETURNTYPE_GOOD = 1;                 // 客户退货通知单_正品退货
    public final static int RETURNRECSHT_RETURNTYPE_BAD = 2;                  // 客户退货通知单_次品退货

    public final static int OUTLETCUSTOMER = 1;                               // 代销客户
    public final static int JXIAOCUSTOMER = 2;                                // 经销客户
    public final static int CXIAOCUSTOMER = 3;                                // 促销客户
    public final static int LYONGCUSTOMER = 4;                                // 领用客户


    public final static int CHEQTYERRADJSHT_SHEETTYPE_OUTLET = 1;             // 代销客户盘点
    public final static int CHEQTYERRADJSHT_SHEETTYPE_WHOUSE = 2;             // 仓库盘点


    public final static int OUTLETRECFTP_SHEETTYPE_RETURNRECSHT = 1;          // 退货到货通知单
    public final static int OUTLETRECFTP_SHEETTYPE_DISSHIPSHT = 2;            // 配货出库单
    public final static int OUTLETRECFTP_SHEETTYPE_CUSTRECQTYERRADJSHT = 3;   // 客户收货数量误差调整单
    public final static int OUTLETRECFTP_SHEETTYPE_RETURNQTYERRADJSHT = 4;    // 客户退货数量误差调整单
    public final static int OUTLETRECFTP_SHEETTYPE_OUTTRANSFERSHT = 5;       // 转货单（转出方记录）
    public final static int OUTLETRECFTP_SHEETTYPE_INTRANSFERSHT = 6;           // 转货单（转入方记录）

    public final static int CUSSAVAMTADJFTP_SHEETTYPE_RETURNSAVAMTADJSHT = 1; // 客户退货储存金额调整单
    public final static int CUSSAVAMTADJFTP_SHEETTYPE_DISREQUESTSHT = 2;      // 配货请求单
    public final static int CUSSAVAMTADJFTP_SHEETTYPE_DISSHT = 3;             // 配货单、
    public final static int CUSSAVAMTADJFTP_SHEETTYPE_DISSHIPSHT = 4;         // 配货出库单
    public final static int CUSSAVAMTADJFTP_SHEETTYPE_CUSTRECSAVAMTADJSHT = 5;// 客户收货储存金额调整单、
    public final static int CUSSAVAMTADJFTP_SHEETTYPE_CUSTINCOMESHT = 6;      // 客户到款单

    public final static int STATUS_UNCOMMIT = 1;                              // 未提交
    public final static int STATUS_COMMIT  = 2;                               // 已经提交

    public final static int SORT_QUALITY_COMMOM_STOCK = 1;                    // 正品一般库位
    public final static int SORT_INFERIOR_COMMOM_STOCK = 2;                   // 次品一般库位
    public final static int SORT_QUALITY_RETURN_STOCK = 3;                    // 正品默认入库库位
    public final static int SORT_INFERIOR_RETURN_STOCK = 4;                   // 次品默认入库库位
    public final static int SORT_QUALITY_OUT_STOCK = 5;                       // 正品默认出库库位
    public final static int SORT_INFERIOR_OUT_STOCK = 6;                      // 次品默认出库库位
    public final static int SORT_QUALITY_ADJ_STOCK = 7;                       // 正品默认调整库位


    public final static int DEALERSALEFTP_SHEETTYPE_RETURNLANDSHT = 1;        // 货入库单、
    public final static int DEALERSALEFTP_SHEETTYPE_DISSHIPSHT = 2;           // 配货出库单、
    public final static int DEALERSALEFTP_SHEETTYPE_CUSTRECQTYERRADJSHT = 3;  // 客户收货数量误差调整单

    public final static int PROMOTIONSALEFTP_SHEETTYPE_RETURNLANDSHT = 1;     // 退货入库单、
    public final static int PROMOTIONSALEFTP_SHEETTYPE_DISSHIPSHT = 2;        // 配货出库单、
    public final static int PROMOTIONSALEFTP_SHEETTYPE_CUSTRECQTYERRADJSHT= 3;// 客户收货数量误差调整单

    public final static int OUTLETSHIPFTP_SHEETTYPE_RETURNLANDSHT = 1;        // 退货入库单、
    public final static int OUTLETSHIPFTP_SHEETTYPE_DISSHIPSHT = 2;           // 配货出库单、
    public final static int OUTLETSHIPFTP_SHEETTYPE_CUSTRECQTYERRADJSHT = 3;  // 客户收货数量误差调整单

    public final static int FREESALEFTP_SHEETTYPE_RETURNLANDSHT = 1;          // 退货入库单、
    public final static int FREESALEFTP_SHEETTYPE_DISSHIPSHT = 2;             // 配货出库单、
    public final static int FREESALEFTP_SHEETTYPE_CUSTRECQTYERRADJSHT = 3;    // 客户收货数量误差调整单
    public final static int FREESALEFTP_SHEETTYPE_WASTECANCELSHT = 4;         // 废品核销单

    public final static int SORT_QUALITY = 0;    // 正品
    public final static int SORT_INFERIOR = 1;   // 次品

}
