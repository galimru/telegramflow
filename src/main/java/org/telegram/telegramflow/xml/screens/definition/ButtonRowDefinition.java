package org.telegram.telegramflow.xml.screens.definition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

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
