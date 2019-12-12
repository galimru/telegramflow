package org.telegram.telegramflow.xml.definition;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "screen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreenDefinition {

    @XmlTransient
    private String id;

    @XmlElement(name = "message")
    private MessageDefinition message;

    @XmlElement(name = "input")
    private InputDefinition input;

    @XmlElement(name = "callback")
    private CallbackDefinition callback;

    @XmlElementWrapper(name = "keyboard")
    @XmlElements({
            @XmlElement(name = "button", type = ButtonDefinition.class),
            @XmlElement(name = "row", type = ButtonRowDefinition.class)
    })
    private List buttons;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageDefinition getMessage() {
        return message;
    }

    public void setMessage(MessageDefinition message) {
        this.message = message;
    }

    public InputDefinition getInput() {
        return input;
    }

    public void setInput(InputDefinition input) {
        this.input = input;
    }

    public CallbackDefinition getCallback() {
        return callback;
    }

    public void setCallback(CallbackDefinition callback) {
        this.callback = callback;
    }

    public List getButtons() {
        return buttons;
    }

    public void setButtons(List buttons) {
        this.buttons = buttons;
    }
}
