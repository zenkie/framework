package nds.security;

import java.io.IOException;
import nds.util.MD5Sum;

public class PwdEncryptor
{

    public PwdEncryptor()
    {
    }

    public static String encrypt(String s, String s1)
        throws IOException
    {
        if(s1 == null)
            s = s;
        else
            s = (new StringBuilder()).append(s1).append(s).toString();
        return MD5Sum.toCheckSumStr(s);
    }
}