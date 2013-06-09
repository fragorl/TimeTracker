package com.fragorl.timetracker.jobs;

import com.fragorl.timetracker.serialization.XmlSerializable;
import com.fragorl.timetracker.time.TimeSegment;
import com.fragorl.timetracker.time.TimeSerialization;
import com.fragorl.timetracker.util.DeEntifyStrings;
import com.fragorl.timetracker.util.EntifyStrings;
import com.fragorl.timetracker.util.RangeUtils;
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

//    private List<TimeSegment> timeSegments = new ArrayList<>();

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

//    public void addTimeSegment(TimeSegment segment) {
//        timeSegments.add(segment);
//    }
//
//    public long timeWorkedMillis() {
//        long totalTime = 0;
//        for (TimeSegment timeSegment : timeSegments) {
//            totalTime += RangeUtils.getLength(timeSegment.asRange());
//        }
//        return totalTime;
//    }

    @Override
    public Element toXml() {
        Element element = new Element(XmlSerializable.ROOT_ELEMENT_NAME);
        element.addContent(new Element("name").setText(name));
        element.addContent(new Element("id").setText(id));
        if (description != null) {
            String descriptionToSerialize = EntifyStrings.entifyXML(description);
            element.addContent(new Element("description").setText(descriptionToSerialize));
        }

//        Element timeSegments = new Element("timeSegments");
//        for (TimeSegment timeSegment : this.timeSegments) {
//            Element serializedTimeSeg = TimeSerialization.serialize(timeSegment, TimeSerialization.StorageType.EpochMillis);
//            serializedTimeSeg.setName("timeSegment");
//            timeSegments.addContent(serializedTimeSeg);
//        }
//        element.addContent(timeSegments);
        return element;
    }

    @Override
    public void fromXml(Element element) {
        this.name = element.getChild("name").getText();
        this.id = element.getChild("id").getText();
        if (element.getChild("description") != null) {
            description = DeEntifyStrings.deEntifyXML(element.getChildText("description"));
        }
//        Element timeSegmentsElements = element.getChild("timeSegments");
//        for (Element child : timeSegmentsElements.getChildren()) {
//            if (child.getName().equals("timeSegment")) {
//                timeSegments.add(TimeSerialization.deserialize(child));
//            }
//        }
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
