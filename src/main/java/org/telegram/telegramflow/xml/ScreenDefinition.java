package org.telegram.telegramflow.xml;

import org.telegram.telegramflow.handlers.UpdateHandler;
import org.telegram.telegramflow.objects.AbstractController;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "screen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreenDefinition {

    @XmlTransient
    private String id;

    @XmlAttribute(name = "class")
    private Class<? extends AbstractController> controllerClass;

    @XmlElement(name = "inputHandler")
    private InputHandlerDefinition inputHandler;

    @XmlElement(name = "inlineHandler")
    private InlineHandlerDefinition inlineHandler;

    @XmlElement(name = "message")
    private String message;

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

    public Class<? extends AbstractController> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<? extends AbstractController> controllerClass) {
        this.controllerClass = controllerClass;
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
