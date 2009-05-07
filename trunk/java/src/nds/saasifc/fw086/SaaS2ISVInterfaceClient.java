
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

public class SaaS2ISVInterfaceClient {

    private static XFireProxyFactory proxyFactory = new XFireProxyFactory();
    private HashMap endpoints = new HashMap();
    private Service service0;

    public SaaS2ISVInterfaceClient() {
        create0();
        Endpoint SaaS2ISVInterfaceSoapLocalEndpointEP = service0 .addEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoapLocalEndpoint"), new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoapLocalBinding"), "xfire.local://SaaS2ISVInterface");
        endpoints.put(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoapLocalEndpoint"), SaaS2ISVInterfaceSoapLocalEndpointEP);
        Endpoint SaaS2ISVInterfaceSoapEP = service0 .addEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoap"), new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoap"), "http://222.92.117.85:7011/SaaS2ISVInterface.asmx");
        endpoints.put(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoap"), SaaS2ISVInterfaceSoapEP);
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
        service0 = asf.create((nds.saasifc.fw086.SaaS2ISVInterfaceSoap.class), props);
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoapLocalBinding"), "urn:xfire:transport:local");
        }
        {
            AbstractSoapBinding soapBinding = asf.createSoap11Binding(service0, new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoap"), "http://schemas.xmlsoap.org/soap/http");
        }
    }

    public SaaS2ISVInterfaceSoap getSaaS2ISVInterfaceSoapLocalEndpoint() {
        return ((SaaS2ISVInterfaceSoap)(this).getEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoapLocalEndpoint")));
    }

    public SaaS2ISVInterfaceSoap getSaaS2ISVInterfaceSoapLocalEndpoint(String url) {
        SaaS2ISVInterfaceSoap var = getSaaS2ISVInterfaceSoapLocalEndpoint();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

    public SaaS2ISVInterfaceSoap getSaaS2ISVInterfaceSoap() {
        return ((SaaS2ISVInterfaceSoap)(this).getEndpoint(new QName("http://SaaSInterface.Sispark.Microsoft.com/", "SaaS2ISVInterfaceSoap")));
    }

    public SaaS2ISVInterfaceSoap getSaaS2ISVInterfaceSoap(String url) {
        SaaS2ISVInterfaceSoap var = getSaaS2ISVInterfaceSoap();
        org.codehaus.xfire.client.Client.getInstance(var).setUrl(url);
        return var;
    }

}
