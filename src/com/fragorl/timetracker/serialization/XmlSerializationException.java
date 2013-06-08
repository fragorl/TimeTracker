package com.fragorl.timetracker.serialization;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 5:21 PM
 */
public class XmlSerializationException extends Exception {
    public XmlSerializationException(String message) {
        super(message);
    }

    public XmlSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
