package com.fragorl.timetracker.serialization;

import org.jdom2.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 28/05/13 5:17 PM
 */
public class XmlSerializer {
    public static final String SERIALIZED_OBJECT_NAME = "fragorlSerializedObject";
    public static final String CLASS_NAME_STRING = "className";

    public static Element classToXml(XmlSerializable serializable) {
        Element element = serializable.toXml();
        element.setName(SERIALIZED_OBJECT_NAME);
        String className = serializable.getClass().getName();
        if (className == null) {
            throw new RuntimeException("Cannot attempt to serialize an object that doesn't have a name");
        }
        element.addContent(new Element(CLASS_NAME_STRING).setText(className));
        return element;
    }

    public static XmlSerializable classFromXml(Element element) throws XmlSerializationException {
        String className = element.getChildText(CLASS_NAME_STRING);
        boolean didItHaveSerializedName = element.getName().equals(SERIALIZED_OBJECT_NAME);
        if (className == null) {
            throw new XmlSerializationException("Could not deserialize element by name of "+element.getName()+", did not have class name."
                + (didItHaveSerializedName ? "" : "Note that it did not have the standard serialized element name either."));
        }
        Class<?> classToInvoke;
        try {
            classToInvoke = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new XmlSerializationException("Name of class \""+className+"\" not found");
        }
        if (!XmlSerializable.class.isAssignableFrom(classToInvoke)) {
            throw new XmlSerializationException("Class of object wasn't an implementer of "+XmlSerializable.class+" (somehow), was "+className);
        }

        Object instance;
        try {
            Constructor<?> constructor = classToInvoke.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (InstantiationException e) {
            if (e.getCause() instanceof NoSuchMethodException && e.getCause().getMessage().contains("<init>()")) {
                throw new XmlSerializationException("ERROR: XmlSerializable class "+ className + " did not have a private empty constructor; it must do!", e);
            }
            throw new XmlSerializationException("Attempt to deserialize an incompatible XmlSerialiable class (e.g. was abstract or something): "+ className, e);
        } catch (IllegalAccessException e) {
            throw new XmlSerializationException("Class did not allow invocation of its constructor: "+className, e);
        } catch (NoSuchMethodException e) {
            throw new XmlSerializationException("Class was missing a defauilt empty constructor: "+className, e);
        } catch (InvocationTargetException e) {
            throw new XmlSerializationException("Class's default constructor threw an exception: "+className, e);
        }

        try {
            Method fromXmlMethod = classToInvoke.getMethod("fromXml", Element.class);
            fromXmlMethod.invoke(instance, element);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unexpected that XmlSerializable didn't have fromXml method!", e);
        } catch (InvocationTargetException e) {
            throw new XmlSerializationException("fromXml threw exception", e);
        } catch (IllegalAccessException e) {
            throw new XmlSerializationException("IllegalAccessException was thrown (could mean lots of things)", e);
        }

        return (XmlSerializable)instance;
    }
}
