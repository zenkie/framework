
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
 *         &lt;element name="SaaSISVInterfaceXMLStringResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "saaSISVInterfaceXMLStringResult"
})
@XmlRootElement(name = "SaaSISVInterfaceXMLStringResponse")
public class SaaSISVInterfaceXMLStringResponse {

    @XmlElement(name = "SaaSISVInterfaceXMLStringResult")
    protected String saaSISVInterfaceXMLStringResult;

    /**
     * Gets the value of the saaSISVInterfaceXMLStringResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaaSISVInterfaceXMLStringResult() {
        return saaSISVInterfaceXMLStringResult;
    }

    /**
     * Sets the value of the saaSISVInterfaceXMLStringResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaaSISVInterfaceXMLStringResult(String value) {
        this.saaSISVInterfaceXMLStringResult = value;
    }

}
