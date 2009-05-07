
package com.microsoft.sispark.saasinterface;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SaaSISVInterfaceResult" type="{http://SaaSInterface.Sispark.Microsoft.com/}ResponseInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "saaSISVInterfaceResult"
})
@XmlRootElement(name = "SaaSISVInterfaceResponse")
public class SaaSISVInterfaceResponse {

    @XmlElement(name = "SaaSISVInterfaceResult")
    protected ResponseInfo saaSISVInterfaceResult;

    /**
     * Gets the value of the saaSISVInterfaceResult property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseInfo }
     *     
     */
    public ResponseInfo getSaaSISVInterfaceResult() {
        return saaSISVInterfaceResult;
    }

    /**
     * Sets the value of the saaSISVInterfaceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseInfo }
     *     
     */
    public void setSaaSISVInterfaceResult(ResponseInfo value) {
        this.saaSISVInterfaceResult = value;
    }

}
