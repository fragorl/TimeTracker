package com.fragorl.timetracker.serialization;

import org.jdom2.Element;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 4:57 PM
 */
public interface XmlSerializable {
    public Element toXml();
    public void fromXml(Element element);
    public static final String ROOT_ELEMENT_NAME = "root";
}
