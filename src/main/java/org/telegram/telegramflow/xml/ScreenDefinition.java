package org.telegram.telegramflow.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "screen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreenDefinition {

    @XmlTransient
    private String id;

    @XmlElement(name = "message")
    private String message;

    @XmlElement(name = "inputHandler")
    private InputHandlerDefinition inputHandler;

    @XmlElement(name = "inlineHandler")
    private InlineHandlerDefinition inlineHandler;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InputHandlerDefinition getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(InputHandlerDefinition inputHandler) {
        this.inputHandler = inputHandler;
    }

    public InlineHandlerDefinition getInlineHandler() {
        return inlineHandler;
    }

    public void setInlineHandler(InlineHandlerDefinition inlineHandler) {
        this.inlineHandler = inlineHandler;
    }

    public List getButtons() {
        return buttons;
    }

    public void setButtons(List buttons) {
        this.buttons = buttons;
    }
}
