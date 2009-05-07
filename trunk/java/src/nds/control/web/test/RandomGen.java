/******************************************************************
*
*$RCSfile: RandomGen.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: RandomGen.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.test;

import java.util.Date;

public class RandomGen {
	public static int getRandomInt(){
		return getInt(0,Integer.MAX_VALUE);
	}
    public static String getRandomString(){
    	return getString(30);
    }
    public static String getRandomString(int maxCount){
    	return getString(maxCount);
    }
    public static long getRandomLong(){
    	return (long)getRandomInt();
    }
    public static short getRandomShort(){
    	return (short) getInt(0,65535);
    }
    public static byte getRandomByte(){
    	return (byte)getInt(0,127);
    }
    public static byte[] getRandomBytes(){
    	byte[] b=new byte[getInt(0, 65535)];
    	for(int i=0;i< b.length;i++){
    		b[i]= getRandomByte();
    	}
    	return b;
    }
    public static double getRandomDouble(){
    	return Math.random() * getRandomInt();
    }
    public static float getRandomFloat(){
    	return (float)getRandomDouble();
    }
    public static java.util.Date getRandomDate(){
		return new Date(getRandomLong()) ;
    }
    public static boolean getRandomBoolean(){
    	return getInt(0,10)>=5;
    }
    public static int getInt(int min, int max){
        return min+ (int)Math.round((Math.random() * (max -min)));
    }

    public static int getInt(int[] range){
        return range[ getInt(0,range.length-1)];
    }
    public static boolean getBoolean(){
        return getInt(0,10)>=5;
    }
    public static Date getDae(Date start, int maxElapDays){
        return new Date(start.getTime()+ getInt( 0,maxElapDays * 24* 3600* 1000));
    }
    public static String getRandomStringOfFixedLength(int length){
    	char[] s=new char[length];
    	for(int i=0;i< length;i++){
    		s[i]=  fake.charAt(getInt(0,fake.length()-1));
    	}
    	return new String(s);
    }
    public static String getString(int maxLength){
        int start=getInt(0,fake.length()-1);
        int end= start+ getInt(0,maxLength);
        if( end> fake.length()-1) end=fake.length()-1;
        return fake.substring(start,end);
    }
    private static final String fake="ADSF-a9FGHY87fgffggfasdDfgft43FjaFfaf"+
    "Fsfj3DFruFpuffdghsdfFafnlshalfgfb8RDvcH32R980fdf-C43832RJerc3JJV4GH343FHafj3r"+
    "30957uf9guad8ucfdvn24tdsdaf-870293ffjfdfdfk8v7 164323139-dsfdfa-tgjtrgyhwgb"+
    "569rsdn4laOIU3WE934J3Ffidfjk39akJIKk9gVGfHJbfGFhKP[0909Hkh";
}