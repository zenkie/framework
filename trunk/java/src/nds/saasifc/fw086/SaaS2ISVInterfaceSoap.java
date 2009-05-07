
package nds.saasifc.fw086;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import com.microsoft.sispark.saasinterface.RequestInfo;
import com.microsoft.sispark.saasinterface.ResponseInfo;

@WebService(name = "SaaS2ISVInterfaceSoap", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
@SOAPBinding(use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface SaaS2ISVInterfaceSoap {


    @WebMethod(operationName = "SaaSISVInterface", action = "http://SaaSInterface.Sispark.Microsoft.com/SaaSISVInterface")
    @WebResult(name = "SaaSISVInterfaceResult", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
    public ResponseInfo saaSISVInterface(
        @WebParam(name = "req", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
        RequestInfo req);

    @WebMethod(operationName = "SaaSISVInterfaceXMLString", action = "http://SaaSInterface.Sispark.Microsoft.com/SaaSISVInterfaceXMLString")
    @WebResult(name = "SaaSISVInterfaceXMLStringResult", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
    public String saaSISVInterfaceXMLString(
        @WebParam(name = "strXML", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
        String strXML);

}
