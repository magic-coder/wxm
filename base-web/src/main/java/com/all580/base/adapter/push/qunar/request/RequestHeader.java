//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.25 at 03:35:22 ���� CST 
//


package com.all580.base.adapter.push.qunar.request;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestHeader", propOrder = {
        "application",
        "processor",
        "version",
        "bodyType",
        "createUser",
        "createTime",
        "supplierIdentity"
})
@Setter
@Getter
public class RequestHeader {
    @XmlElement(required = true, defaultValue = "Qunar.Menpiao.Agent")
    protected String application;
    @XmlElement(required = true, defaultValue = "SupplierDataExchangeProcessor")
    protected String processor;
    @XmlElement(required = true, defaultValue = "v2.0.1")
    protected String version;
    @XmlElement(required = true)
    protected String bodyType;
    @XmlElement(required = true)
    protected String createUser;
    @XmlElement(required = true)
    protected String createTime;
    @XmlElement(required = true)
    protected String supplierIdentity;
}
