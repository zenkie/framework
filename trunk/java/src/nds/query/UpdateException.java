package nds.query;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class UpdateException extends NDSException{

    public UpdateException() {
    }


    public UpdateException(String s)
    {
        super(s);
    }

    public UpdateException(String s, Exception exception)
    {
        super(s,exception);
    }
}