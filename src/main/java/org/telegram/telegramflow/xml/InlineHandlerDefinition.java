package org.telegram.telegramflow.xml;

import org.telegram.telegramflow.handlers.UpdateHandler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "inlineHandler")
@XmlAccessorType(XmlAccessType.FIELD)
public class InlineHandlerDefinition {

    @XmlAttribute(name = "class", required = true)
    private Class<? extends UpdateHandler> handlerClass;

    public Class<? extends UpdateHandler> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<? extends UpdateHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }
}
