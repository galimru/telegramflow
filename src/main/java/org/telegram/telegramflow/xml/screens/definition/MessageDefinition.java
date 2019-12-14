package org.telegram.telegramflow.xml.screens.definition;

import org.telegram.telegramflow.objects.FormatType;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDefinition {

    @XmlValue
    private String text;

    @XmlAttribute(name = "format")
    private FormatType format;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }
}
