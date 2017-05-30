package com.getMAC;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.util.AES;

public class GetMACH {
	/**
	 * 利用CPU个数与所有的MAC地址生成一个字符串，中间用,分开
	 * @return 机器的唯一标示码
	 */
	
	private static  String a;
	
    public static void setA(String a) {
		GetMACH.a = a;
	}

	public static String getMach() {
    	StringBuffer MACstr = new StringBuffer();
        try {
             //cpu数量
        	int cpuThreadNum = cpuThreadNum();
        	MACstr.append(String.valueOf(cpuThreadNum));
            // 以太网信息
        	//MACstr = ethernet(MACstr);
        	String sign=nds.util.MD5Sum.toCheckSumStr(NetInterfaceData.getCPUSerial()+NetInterfaceData.getHardDiskSN("C")+NetInterfaceData.getMotherboardSN());
        	MACstr.append(","+sign);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return MACstr.toString();
    }

    /**
     * CPU的数量
     * @return threadNum CPU个数
     * @throws SigarException
     */

    private static int cpuThreadNum() throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo infos[] = sigar.getCpuInfoList();
        int threadNum = infos.length;
        return threadNum;
        }
    /**
     * MAC地址
     * @param MACstr 
     * @return 所有MAC地址
     * @throws SigarException
     */
    @Deprecated
    private static StringBuffer ethernet(StringBuffer MACstr) throws SigarException {
        Sigar sigar = null;
        String currStr = null;
        sigar = new Sigar();
        String[] ifaces = sigar.getNetInterfaceList();
        for (int i = 0; i < ifaces.length; i++) {
            NetInterfaceConfig cfg = sigar.getNetInterfaceConfig(ifaces[i]);
            System.out.println(cfg.getAddress());
            System.out.println(cfg.getFlags());
            if (NetFlags.LOOPBACK_ADDRESS.equals(cfg.getAddress()) || (cfg.getFlags() & NetFlags.IFF_LOOPBACK) != 0
                    || NetFlags.NULL_HWADDR.equals(cfg.getHwaddr())) {
                continue;
            }
            currStr = ","+cfg.getHwaddr();
//                网卡MAC地址
            if(MACstr.indexOf(currStr) > 0){
            	continue;
            } else{
            	MACstr.append(currStr);
            }
        }

        System.out.print(MACstr);
        return MACstr;
    }
    
    /**
     * get_mac addresss for aes
     * move licenseManager
     * @throws Exception 
     */
    @Deprecated
    public static String  get_maconly(String pwd) throws Exception{
    	AES aes=new AES(pwd);
    	String str = aes.encrypt(GetMACH.getMach());
    	return str;
    }
}
