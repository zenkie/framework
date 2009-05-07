
package com.microsoft.sispark.saasinterface;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.microsoft.sispark.saasinterface package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.microsoft.sispark.saasinterface
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResponseInfo }
     * 
     */
    public ResponseInfo createResponseInfo() {
        return new ResponseInfo();
    }

    /**
     * Create an instance of {@link ResponseHeadInfo }
     * 
     */
    public ResponseHeadInfo createResponseHeadInfo() {
        return new ResponseHeadInfo();
    }

    /**
     * Create an instance of {@link RequestHeadInfo }
     * 
     */
    public RequestHeadInfo createRequestHeadInfo() {
        return new RequestHeadInfo();
    }

    /**
     * Create an instance of {@link RequestInfo }
     * 
     */
    public RequestInfo createRequestInfo() {
        return new RequestInfo();
    }

    /**
     * Create an instance of {@link SaaSISVInterfaceResponse }
     * 
     */
    public SaaSISVInterfaceResponse createSaaSISVInterfaceResponse() {
        return new SaaSISVInterfaceResponse();
    }

    /**
     * Create an instance of {@link SaaSISVInterfaceXMLStringResponse }
     * 
     */
    public SaaSISVInterfaceXMLStringResponse createSaaSISVInterfaceXMLStringResponse() {
        return new SaaSISVInterfaceXMLStringResponse();
    }

    /**
     * Create an instance of {@link SaaSISVInterfaceXMLString }
     * 
     */
    public SaaSISVInterfaceXMLString createSaaSISVInterfaceXMLString() {
        return new SaaSISVInterfaceXMLString();
    }

    /**
     * Create an instance of {@link SaaSISVInterface }
     * 
     */
    public SaaSISVInterface createSaaSISVInterface() {
        return new SaaSISVInterface();
    }

}
