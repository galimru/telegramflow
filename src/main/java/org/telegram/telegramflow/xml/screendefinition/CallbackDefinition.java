package org.telegram.telegramflow.xml.screendefinition;

import org.telegram.telegramflow.handlers.UpdateHandler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class CallbackDefinition {

    @XmlAttribute(name = "handler", required = true)
    private Class<? extends UpdateHandler> handlerClass;

    public Class<? extends UpdateHandler> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<? extends UpdateHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }
}
