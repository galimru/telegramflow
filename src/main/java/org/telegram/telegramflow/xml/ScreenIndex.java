package org.telegram.telegramflow.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "screen")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreenIndex {

    @XmlAttribute(name = "id", required = true)
    private String id;

    @XmlAttribute(name = "src", required = true)
    private String src;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
