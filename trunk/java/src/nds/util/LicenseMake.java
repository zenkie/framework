package nds.util;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class LicenseMake  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract void validateLicense(String product, String version, String licenseFile);

	
	public abstract Iterator getLicenses();
	
	public abstract boolean isValidProduct(String product, License license);
//	
//	
//	public abstract String getPwd();
	
	public abstract String get_maconly();
	
	public abstract void setPwd(String string);
	
	
	
}
