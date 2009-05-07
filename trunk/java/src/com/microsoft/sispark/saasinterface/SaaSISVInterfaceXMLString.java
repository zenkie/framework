
package com.microsoft.sispark.saasinterface;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="strXML" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "strXML"
})
@XmlRootElement(name = "SaaSISVInterfaceXMLString")
public class SaaSISVInterfaceXMLString {

    protected String strXML;

    /**
     * Gets the value of the strXML property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrXML() {
        return strXML;
    }

    /**
     * Sets the value of the strXML property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrXML(String value) {
        this.strXML = value;
    }

}
