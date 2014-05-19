package com.getMAC;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import nds.util.AES;

public class GetMACH {
	/**
	 * ����CPU���������е�MAC��ַ����һ���ַ������м���,�ֿ�
	 * @return ������Ψһ��ʾ��
	 */
    public static String getMach() {
    	StringBuffer MACstr = new StringBuffer();
        try {
             //cpu����
        	int cpuThreadNum = cpuThreadNum();
        	MACstr.append(String.valueOf(cpuThreadNum));
            // ��̫����Ϣ
        	MACstr = ethernet(MACstr);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return MACstr.toString();
    }

    /**
     * CPU������
     * @return threadNum CPU����
     * @throws SigarException
     */
    private static int cpuThreadNum() throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo infos[] = sigar.getCpuInfoList();
        int threadNum = infos.length;
        return threadNum;
        }
    /**
     * MAC��ַ
     * @param MACstr 
     * @return ����MAC��ַ
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
//                ����MAC��ַ
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
    	AES aes=new AES("bosxe");
    	String str = aes.encrypt(GetMACH.getMach()+";");
    	return str;
    }
}
