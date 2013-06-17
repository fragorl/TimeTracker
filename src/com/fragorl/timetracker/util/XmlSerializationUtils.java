package com.fragorl.timetracker.util;

import com.fragorl.timetracker.serialization.XmlSerializable;
import com.fragorl.timetracker.serialization.XmlSerializationException;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 17/06/13 9:42 AM
 */
public class XmlSerializationUtils {

    private static final String ROOT_ELEMENT_STRING_LIST_NAME = "stringList";
    private static final String NULL_LIST_ITEM_ELEMENT_NAME = "nullValue";
    private static final String NONNULL_LIST_ITEM_ELEMENT_NAME = "value";

    private XmlSerializationUtils() {}

    public static Element stringListToElement(List<String> toSerialize) {
        Element result = new Element(ROOT_ELEMENT_STRING_LIST_NAME);
        for (String listItem : toSerialize) {
            if (listItem == null) {
                result.addContent(new Element(NULL_LIST_ITEM_ELEMENT_NAME));
            } else {
                String entifiedListItem = EntifyStrings.entifyXML(listItem);
                result.addContent(new Element(NONNULL_LIST_ITEM_ELEMENT_NAME).setText(entifiedListItem));
            }
        }
        return result;
    }

    public static List<String> elementToStringList(Element toDeserialize) throws XmlSerializationException {
        List<String> result = new ArrayList<>();
        for (Element child : toDeserialize.getChildren()) {
            if        (NULL_LIST_ITEM_ELEMENT_NAME.equals(child.getName())) {
                result.add(null);
            } else if (NONNULL_LIST_ITEM_ELEMENT_NAME.equals(child.getName())) {
                result.add(DeEntifyStrings.deEntifyXML(child.getText()));
            } else {
                throw new XmlSerializationException("Unknown element child name "+child.getName());
            }
        }
        return result;
    }
}
