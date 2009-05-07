
package nds.saasifc.fw086;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import com.microsoft.sispark.saasinterface.RequestInfo;
import com.microsoft.sispark.saasinterface.ResponseInfo;

@WebService(name = "ISV2SaaSInterfaceSoap", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
@SOAPBinding(use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface ISV2SaaSInterfaceSoap {


    @WebMethod(operationName = "ISVSaaSInterface", action = "http://SaaSInterface.Sispark.Microsoft.com/ISVSaaSInterface")
    @WebResult(name = "ISVSaaSInterfaceResult", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
    public ResponseInfo iSVSaaSInterface(
        @WebParam(name = "req", targetNamespace = "http://SaaSInterface.Sispark.Microsoft.com/")
        RequestInfo req);

}
