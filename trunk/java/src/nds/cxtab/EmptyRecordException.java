package nds.cxtab;

import nds.util.NDSException;

public class EmptyRecordException extends NDSException {
	public EmptyRecordException() {
	}

	public EmptyRecordException(String paramString) {
		super(paramString);
	}

	public EmptyRecordException(String paramString, Exception paramException) {
		super(paramString, paramException);
	}
}