package org.telegram.telegramflow.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "screens")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScreensDescriptor {

    @XmlElement(name = "screen")
    private List<ScreenIndex> screens;

    public List<ScreenIndex> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenIndex> screens) {
        this.screens = screens;
    }
}
