package com.fragorl.timetracker.jobs;

import com.fragorl.timetracker.serialization.XmlSerializable;
import com.fragorl.timetracker.serialization.XmlSerializationException;
import com.fragorl.timetracker.time.TimeSegment;
import com.fragorl.timetracker.time.TimeSerialization;
import com.fragorl.timetracker.util.DeEntifyStrings;
import com.fragorl.timetracker.util.EntifyStrings;
import com.fragorl.timetracker.util.RangeUtils;
import com.fragorl.timetracker.util.XmlSerializationUtils;
import com.sun.istack.internal.Nullable;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 23/05/13 10:09 AM
 */
public class Job implements XmlSerializable {
    private String name;
    private String id;
    private @Nullable String description;
    private @Nullable String parentTaskId;

    private Job() {
        // for xml deserialization
    }

    Job(String name, String id, @Nullable String description) {
        this.name = name;
        this.id = id;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public @Nullable String getParentTaskId() {
        return parentTaskId;
    }

    @Override
    public Element toXml() {
        Element element = new Element(XmlSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element("name").setText(EntifyStrings.entifyXML(name)));
        element.addContent(new Element("id").setText(EntifyStrings.entifyXML(id)));
        if (description != null) {
            String descriptionToSerialize = EntifyStrings.entifyXML(description);
            element.addContent(new Element("description").setText(descriptionToSerialize));
        }
        if (parentTaskId != null) {
            element.addContent(new Element("parentTaskId").setText(parentTaskId));
        }
        return element;
    }

    @Override
    public void fromXml(Element element) throws XmlSerializationException {
        this.name = DeEntifyStrings.deEntifyXML(element.getChild("name").getText());
        this.id = DeEntifyStrings.deEntifyXML(element.getChild("id").getText());
        if (element.getChild("description") != null) {
            description = DeEntifyStrings.deEntifyXML(element.getChildText("description"));
        }
        Element parentTaskIdElement = element.getChild("parentTaskId");
        if (parentTaskIdElement != null) {
            parentTaskId = DeEntifyStrings.deEntifyXML(parentTaskIdElement.getText());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return id.equals(job.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
