package com.getMAC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

    public class NetInterfaceData {  
      
        /** 
         * Return Opertaion System Name; 
         *  
         * @return os name. 
         */  
        public static String getOsName() {  
            String os = "";  
            os = System.getProperty("os.name");  
            //System.out.print(os);
            return os;  
        }  
      
        /** 
         * Returns the MAC address of the computer. 
         *  
         * @return the MAC address 
         */  
        public static String getMACAddress() {  
            String address = "";  
            String os = getOsName();  
            if (os.startsWith("Windows")) {  
                try {  
                    String command = "cmd.exe /c ipconfig /all";  
                    Process p = Runtime.getRuntime().exec(command);  
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                    String line;  
                    while ((line = br.readLine()) != null) {  
                        if (line.indexOf("Physical Address") > 0) {  
                            int index = line.indexOf(":");  
                            index += 2;  
                            address = line.substring(index);  
                            break;  
                        }else  if (line.indexOf("�����ַ") > 0) {  
                            int index = line.indexOf(":");  
                            index += 2;  
                            address = line.substring(index);  
                            break;  
                        }  
                    }  
                    br.close();  
                    return address.trim();  
                } catch (IOException e) {  
                }  
            } else if (os.startsWith("Linux")) {  
            	//System.out.print(os);
                String command = "/bin/sh -c ifconfig -a";  
                Process p;  
                try {  
                    p = Runtime.getRuntime().exec(command);  
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                    String line;  
                    while ((line = br.readLine()) != null) {  
                    	//System.out.print(line);
                        if (line.indexOf("HWaddr") > 0) {  
                            int index = line.indexOf("HWaddr") + "HWaddr".length();  
                            address = line.substring(index);  
                            break;  
                        }  
                    }  
                    br.close();  
                } catch (IOException e) {  
                	System.out.print("error");
                }  
            }  
            address = address.trim();  
           // System.out.print(address);
            return address;  
        }  
          
          
          /** 
         * ��ȡCPU��,��CPUʱ,ֻȡ��һ�� 
         * @return 
         */  
        public static String getCPUSerial() {  
            String result = "";  
            String os = getOsName();  
            if (os.startsWith("Windows")) {  
            try {  
                File file = File.createTempFile("tmp", ".vbs");  
                file.deleteOnExit();  
                FileWriter fw = new java.io.FileWriter(file);  
      
                String vbs = "On Error Resume Next \r\n\r\n" + "strComputer = \".\"  \r\n"  
                        + "Set objWMIService = GetObject(\"winmgmts:\" _ \r\n"  
                        + "    & \"{impersonationLevel=impersonate}!\\\\\" & strComputer & \"\\root\\cimv2\") \r\n"  
                        + "Set colItems = objWMIService.ExecQuery(\"Select * from Win32_Processor\")  \r\n "  
                        + "For Each objItem in colItems\r\n " + "    Wscript.Echo objItem.ProcessorId  \r\n "  
                        + "    exit for  ' do the first cpu only! \r\n" + "Next                    ";  
      
                fw.write(vbs);  
                fw.close();  
                Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());  
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                String line;  
                while ((line = input.readLine()) != null) {  
                    result += line;  
                }  
                input.close();  
                file.delete();  
            } catch (Exception e) {  
                e.fillInStackTrace();  
            }  
            }else if (os.startsWith("Linux")) {  
                String CPU_ID_CMD = "dmidecode -t 4 | grep ID |sort -u |awk -F': ' '{print $2}'";  
                 Process p;  
                 try {  
                     p = Runtime.getRuntime().exec(new String[]{"sh","-c",CPU_ID_CMD});//�ܵ�  
                     BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                     String line;  
                     while ((line = br.readLine()) != null) {  
                             result += line;  
                             break;  
                     }  
                     br.close();  
                 } catch (IOException e) {  
                 }  
            }else if (os.startsWith("Mac")) {
            	 Process p;  
                 try {  
                     p = Runtime.getRuntime().exec(new String[]{"sh","-c","ioreg -rd1 -c IOPlatformExpertDevice | awk '/IOPlatformUUID/ { print $3; }'"});//�ܵ�  
                     BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));  
                     //System.out.print(br.toString());
                     String line;  
                     while ((line = br.readLine()) != null) {  
                    	 //System.out.print(line);
                             result += line;  
                             break;  
                     }  
                     br.close();  
                 } catch (IOException e) {  
                 }  
            }
            
            if (result.trim().length() < 1 || result == null) {  
                result = "��CPU_ID����ȡ";  
            }  
            return result.trim().replace("\"","");//.replace("-", "");  
        }  
          
      
          
        /** 
         * Main Class. 
         *  
         * @param args 
         * @throws NoSuchPaddingException  
         * @throws NoSuchAlgorithmException  
         */  
        public static void main(String[] args) throws Exception {  
      
            String macAddress=NetInterfaceData.getMACAddress();  
            String cpuSerial=NetInterfaceData.getCPUSerial();  
            String test =cpuSerial;
            System.out.print(test);
      
             
              
           } 
        
    }