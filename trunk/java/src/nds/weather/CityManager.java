package nds.weather;

import java.util.Iterator;
import java.util.Properties;
/**
 * City infomation including code, name, pingyin
 */
public final class CityManager {
    private static CityManager instance=null;
    private Properties pc=new Properties();//key:pingyin, value:code
    private Properties pn=new Properties();//key:pingyin, value:name in chinese
    private CityManager() {
        for(int i=0;i<cityInfo.length;i+=3){
            pn.setProperty(cityInfo[i+1].toLowerCase() , cityInfo[i]);
            pc.setProperty(cityInfo[i+1].toLowerCase() , cityInfo[i+2]);
        }
    }
    public String getCityCode(String pingyin){
        return pc.getProperty(pingyin.toLowerCase() , pingyin);
    }
    public String getCityNameInChinese(String pingyin){
        return pn.getProperty(pingyin.toLowerCase() , pingyin);

    }
    public static CityManager getInstance(){
        if(instance==null) instance= new CityManager();
        return instance;
    }
    public void dumpAll(){
        for(Iterator it=pc.keySet().iterator();it.hasNext();){
            String key=(String) it.next();
            System.out.println(key+","+ pc.getProperty(key)+","+ pn.getProperty(key));
        }
    }
    public static void  main(String[] args){
        CityManager.getInstance().dumpAll();
    }
    private static String[] cityInfo={
            "北京","BeiJing","CHXX0008",
            "上海","ShangHai","CHXX0116",
            "广州","GuangZhou","CHXX0037",
            "安顺","AnShun","CHXX0005",
            "保定","BaoDing","CHXX0308",
            "保山","BaoShan","CHXX0370",
            "长沙","ChangSha","CHXX0013",
            "长春","ChangChun","CHXX0010",
            "常州","ChangZhou","CHXX0015",
            "重庆","ZhongQing","CHXX0017",
            "成都","ChengDu","CHXX0016",
            "赤峰","ChiFeng","CHXX0286",
            "大连","DaLian","CHXX0019",
            "大里","DaLi","CHXX0371",
            "大同","DaTong","CHXX0251",
            "佛山","FoShan","CHXX0028",
            "抚顺","FuShun","CHXX0029",
            "福州","FuZhou","CHXX0031",
            "高雄","GaoXiong","TWXX0013",
            "桂林","GuiLin","CHXX0434",
            "贵阳","GuiYang","CHXX0039",
            "哈尔滨","HaErbin","CHXX0046",
            "海口","HaiKou","CHXX0502",
            "杭州","HangZhou","CHXX0044",
            "合肥","HeFei","CHXX0448",
            "惠州","HuiZhou","CHXX0053",
            "吉林","JiLin","CHXX0063",
            "济南","JiNan","CHXX0064",
            "九江","JiuJiang","CHXX0068",
            "开封","KaiFeng","CHXX0072",
            "昆明","KunMing","CHXX0076",
            "拉萨","LaSa","CHXX0080",
            "兰州","LanZhou","CHXX0079",
            "洛阳","LuoYang","CHXX0086",
            "柳州","LiuZhou","CHXX0479",
            "南昌","NanChang","CHXX0097",
            "南京","NanJing","CHXX0099",
            "南宁","NanNing","CHXX0100",
            "南通","NanTong","CHXX0101",
            "绵阳","MianYang","CHXX0351",
            "牡丹江","MuDanjiang","CHXX0278",
            "青岛","QingDao","CHXX0110",
            "泉州","QuanZhou","CHXX0114",
            "绍兴","ShaoXing","CHXX0117",
            "汕头","ShanTou","CHXX0493",
            "沈阳","ShenYang","CHXX0119",
            "深圳","ShenZhen","CHXX0120",
            "石家庄","ShiJiazhuang","CHXX0122",
            "太原","TaiYuan","CHXX0129",
            "台北","TaiBei","TWXX0021",
            "台中","TaiZhong","TWXX0019",
            "天津","TianJin","CHXX0133",
            "温州","WenZhou","CHXX0462",
            "乌鲁木齐","WuLumuqi","CHXX0135",
            "西安","XiAn","CHXX0141",
            "西宁","XiNing","CHXX0236",
            "厦门","XiaMen","CHXX0140",
            "香港","XiangGang","CHXX0049",
            "咸阳","XianYang","CHXX0143",
            "新乡","XinXiang","CHXX0148",
            "新竹","XinZhu","TWXX0009",
            "徐州","XuZhou","CHXX0437",
            "武汉","WuHan","CHXX0138",
            "武夷山","WuYishan","CHXX0467",
            "延吉","YanJi","CHXX0291",
            "宜昌","YiChang","CHXX0407",
            "宜宾","YiBin","CHXX0362",
            "伊宁","YiNing","CHXX0203",
            "银川","YinChuan","CHXX0259",
            "岳阳","YueYang","CHXX0411",
            "张家口","ZhangJiakou","CHXX0300",
            "郑州","ZhengZhou","CHXX0165"
    };
}