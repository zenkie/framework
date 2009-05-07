
package nds.saasifc.fw086;

import javax.jws.WebService;
import com.microsoft.sispark.saasinterface.RequestInfo;
import com.microsoft.sispark.saasinterface.ResponseInfo;

@WebService(serviceName = "SaaS2ISVInterface", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/", endpointInterface = "nds.saasifc.fw086.SaaS2ISVInterfaceSoap")
public class SaaS2ISVInterfaceImpl
    implements SaaS2ISVInterfaceSoap
{


    public ResponseInfo saaSISVInterface(RequestInfo req) {
        throw new UnsupportedOperationException();
    }

    public String saaSISVInterfaceXMLString(String strXML) {
        throw new UnsupportedOperationException();
    }

}
