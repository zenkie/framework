package com.getMAC;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import nds.util.AES;

public class GetMACH {
	/**
	 * 利用CPU个数与所有的MAC地址生成一个字符串，中间用,分开
	 * @return 机器的唯一标示码
	 */
    public static String getMach() {
    	StringBuffer MACstr = new StringBuffer();
        try {
             //cpu数量
        	int cpuThreadNum = cpuThreadNum();
        	MACstr.append(String.valueOf(cpuThreadNum));
            // 以太网信息
        	MACstr = ethernet(MACstr);
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
    private static StringBuffer ethernet(StringBuffer MACstr) throws SigarException {
        Sigar sigar = null;
        String currStr = null;
        sigar = new Sigar();
        String[] ifaces = sigar.getNetInterfaceList();
        for (int i = 0; i < ifaces.length; i++) {
            NetInterfaceConfig cfg = sigar.getNetInterfaceConfig(ifaces[i]);
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
        return MACstr;
    }
    
    /**
     * get_mac addresss for aes
     * @throws Exception 
     */
    public static String  get_maconly() throws Exception{
    	AES aes=new AES("burgeon");
    	String str = aes.encrypt(GetMACH.getMach());
    	return str;
    }
}
