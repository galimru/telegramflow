package org.telegram.telegramflow.xml.screendefinition;

import org.telegram.telegramflow.handlers.UpdateHandler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ButtonDefinition {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "transitTo")
    private String transitTo;

    @XmlAttribute(name = "handler")
    private Class<? extends UpdateHandler> handlerClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransitTo() {
        return transitTo;
    }

    public void setTransitTo(String transitTo) {
        this.transitTo = transitTo;
    }

    public Class<? extends UpdateHandler> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<? extends UpdateHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }
}
