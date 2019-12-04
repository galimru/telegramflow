package org.telegram.telegramflow.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "row")
@XmlAccessorType(XmlAccessType.FIELD)
public class ButtonRowDefinition {

    @XmlElement(name = "button")
    private List<ButtonDefinition> buttons;

    public List<ButtonDefinition> getButtons() {
        return buttons;
    }

    public void setButtons(List<ButtonDefinition> buttons) {
        this.buttons = buttons;
    }
}
