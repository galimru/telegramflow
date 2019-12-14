package org.telegram.telegramflow.xml.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "actions")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionsDescriptor {

    @XmlElement(name = "action")
    private List<ActionIndex> actions;

    public List<ActionIndex> getActions() {
        return actions;
    }

    public void setActions(List<ActionIndex> actions) {
        this.actions = actions;
    }
}
