package com.fragorl.timetracker.time;

import com.fragorl.timetracker.serialization.XmlSerializationException;
import org.jdom2.Element;

import java.math.BigInteger;


/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 5:07 PM
 */
public class TimeSerialization {
    public static final String ELEMENT_NAME = "TimeSegment";
    public static final String STORAGE_TYPE_LABEL = "storageType";
    public enum StorageType {
        EpochMillis() {
            @Override
            public Element toXml(TimeSegment timeSegment) {
                Element result = new Element(ELEMENT_NAME);
                result.addContent(new Element(STORAGE_TYPE_LABEL).setText(StorageType.EpochMillis.name()));
                result.addContent(new Element("from").setText(timeSegment.fromInclusive + ""));
                result.addContent(new Element("to").setText(timeSegment.toExclusive+""));
                return result;
            }

            @Override
            public TimeSegment fromXml(Element element) {
                long from = new BigInteger(element.getChild("from").getText()).longValue();
                long to = new BigInteger(element.getChild("from").getText()).longValue();
                return new TimeSegment(from, to);
            }
        };

        abstract Element toXml(TimeSegment timeSegment);
        abstract TimeSegment fromXml(Element element);
    }

    public static Element serialize(TimeSegment timeSegment, StorageType storageType) {
        return storageType.toXml(timeSegment);
    }

    public static TimeSegment deserialize(Element element) throws XmlSerializationException {
        Element storageTypeElement = element.getChild(STORAGE_TYPE_LABEL);
        if (storageTypeElement == null) {
            throw new XmlSerializationException("No storage type details");
        }
        String storageTypeText = storageTypeElement.getText();
        if (storageTypeText.isEmpty()) {
            throw new XmlSerializationException("No storage type name specified");
        }

        for (StorageType storageType : StorageType.values()) {
            if (storageTypeText.equals(storageType.name())) {
                return storageType.fromXml(element);
            }
        }
        throw new XmlSerializationException("No unknown time storage type "+storageTypeText);
    }
}
