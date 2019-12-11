package org.telegram.telegramflow.xml;

import org.telegram.telegramflow.objects.AbstractController;

import javax.xml.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "screen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreenDefinition implements Cloneable {

    @XmlTransient
    private String id;

    @XmlAttribute(name = "controller")
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

    @Override
    public ScreenDefinition clone() {
        ScreenDefinition clone;
        try {
            clone = (ScreenDefinition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(String.format("Cannot clone screen definition %s",
                    this), e);
        }
        clone.setId(id);
        clone.setMessage(message);
        clone.setControllerClass(controllerClass);
        clone.setInlineHandler(inlineHandler);
        clone.setInputHandler(inputHandler);
        if (buttons != null) {
            clone.setButtons(Collections.unmodifiableList(buttons));
        }
        return clone;
    }
}
