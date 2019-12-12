package org.telegram.telegramflow.xml.definition;

import org.telegram.telegramflow.handlers.KeyboardAction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ButtonDefinition {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "transitTo")
    private String transitTo;

    @XmlAttribute(name = "action")
    private Class<? extends KeyboardAction> action;

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

    public Class<? extends KeyboardAction> getAction() {
        return action;
    }

    public void setAction(Class<? extends KeyboardAction> action) {
        this.action = action;
    }
}
