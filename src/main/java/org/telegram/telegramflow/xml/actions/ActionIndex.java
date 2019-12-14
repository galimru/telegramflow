package org.telegram.telegramflow.xml.actions;

import org.telegram.telegramflow.handlers.CallbackAction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActionIndex {

    @XmlAttribute(name = "key", required = true)
    private String key;

    @XmlAttribute(name = "class", required = true)
    private Class<? extends CallbackAction> actionClass;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Class<? extends CallbackAction> getActionClass() {
        return actionClass;
    }

    public void setActionClass(Class<? extends CallbackAction> actionClass) {
        this.actionClass = actionClass;
    }
}
