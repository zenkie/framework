
package nds.saasifc.fw086;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.jsr181.Jsr181WebAnnotations;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.jaxb2.JaxbTypeRegistry;
import org.codehaus.xfire.service.Endpoint;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.transport.TransportManager;

public class ISV2SaaSInterfaceClient {

    private static XFireProxyFactory proxyFactory = new XFireProxyFactory();
    private HashMap endpoints = new HashMap();
    private Service service0;

    public ISV2SaaSInterfaceClient() {
        create0();
        Endpoint ISV2SaaSInterfaceSoapEP = service0 .addEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoap"), new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoap"), "http://222.92.117.85:7001/ISV2SaaSInterface.asmx");
        endpoints.put(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoap"), ISV2SaaSInterfaceSoapEP);
        Endpoint ISV2SaaSInterfaceSoapLocalEndpointEP = service0 .addEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoapLocalEndpoint"), new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoapLocalBinding"), "xfire.local://ISV2SaaSInterface");
        endpoints.put(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoapLocalEndpoint"), ISV2SaaSInterfaceSoapLocalEndpointEP);
    }

    public Object getEndpoint(Endpoint endpoint) {
        try {
            return proxyFactory.create((endpoint).getBinding(), (endpoint).getUrl());
        } catch (MalformedURLException e) {
            throw new XFireRuntimeException("Invalid URL", e);
        }
    }

    public Object getEndpoint(QName name) {
        Endpoint endpoint = ((Endpoint) endpoints.get((name)));
        if ((endpoint) == null) {
            throw new IllegalStateException("No such endpoint!");
        }
        return getEndpoint((endpoint));
    }

    public Collection getEndpoints() {
        return endpoints.values();
    }

    private void create0() {
        TransportManager tm = (org.codehaus.xfire.XFireFactory.newInstance().getXFire().getTransportManager());
        HashMap props = new HashMap();
        props.put("annotations.allow.interface", true);
        AnnotationServiceFactory asf = new AnnotationServiceFactory(new Jsr181WebAnnotations(), tm, new AegisBindingProvider(new JaxbTypeRegistry()));
        asf.setBindingCreationEnabled(false);
        service0 = asf.create((nds.saasifc.fw086.ISV2SaaSInterfaceSoap.class), props);
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoap"), "http://schemas.xmlsoap.org/soap/http");
        }
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoapLocalBinding"), "urn:xfire:transport:local");
        }
    }

    public ISV2SaaSInterfaceSoap getISV2SaaSInterfaceSoap() {
        return ((ISV2SaaSInterfaceSoap)(this).getEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoap")));
    }

    public ISV2SaaSInterfaceSoap getISV2SaaSInterfaceSoap(String url) {
        ISV2SaaSInterfaceSoap var = getISV2SaaSInterfaceSoap();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

    public ISV2SaaSInterfaceSoap getISV2SaaSInterfaceSoapLocalEndpoint() {
        return ((ISV2SaaSInterfaceSoap)(this).getEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "ISV2SaaSInterfaceSoapLocalEndpoint")));
    }

    public ISV2SaaSInterfaceSoap getISV2SaaSInterfaceSoapLocalEndpoint(String url) {
        ISV2SaaSInterfaceSoap var = getISV2SaaSInterfaceSoapLocalEndpoint();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

}
