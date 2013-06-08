package com.fragorl.timetracker.persistence;

import com.fragorl.timetracker.jobs.Job;
import com.fragorl.timetracker.serialization.XmlSerializable;
import com.fragorl.timetracker.serialization.XmlSerializationException;
import com.fragorl.timetracker.serialization.XmlSerializer;
import com.fragorl.timetracker.util.SystemUtils;
import com.sun.istack.internal.Nullable;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 * @version $Id$
 *          <p/>
 *          Created on 27/05/13 5:52 PM
 */
public class PersistenceManager {
    private static final String FILE_EXTENSION = "wlog.xml";
    public static final String DATA_DIRECTORY_NAME = "com.fragorl.TimeTracker";

    private static final String ROOT_ELEMENT_NAME = "root";
    private static final String ACTIVE_JOB_ELEMENT_NAME = "activeJob";
    private static final String JOBS_ELEMENT_NAME = "jobs";

    private static final PersistenceManager instance = new PersistenceManager();

    private File currentDatabaseFile;
    private Document currentDatabaseDocument;

    private PersistenceManager() {
        File baseDir;
        if (SystemUtils.isWindows()) {
            baseDir = new File(System.getenv("AppData"));
        } else if (SystemUtils.isMac() || SystemUtils.isLinux()) {
            baseDir = new File(System.getProperty("user.home"));
        } else {
            throw new RuntimeException("Unsupported OS!");
        }

        // create our data directory
        File dataDirectory = new File(baseDir, DATA_DIRECTORY_NAME);
        if (dataDirectory.exists() && !dataDirectory.isDirectory()) {
            throw new RuntimeException("A non-directory file exists with the data directory name that we want, "+DATA_DIRECTORY_NAME);
        }
        //noinspection ResultOfMethodCallIgnored
        dataDirectory.mkdirs();

        //noinspection ConstantConditions
        // the directory always exists here so listfiles will never return null
        File[] dataDirContents = dataDirectory.listFiles();
        if (dataDirContents == null) {
            throw new RuntimeException("listfiles returned null for data dir contents, this should be impossible");
        }

        String defaultFileName = "default."+FILE_EXTENSION;
        for (File file : dataDirContents) {
            if (file.getName().equals(defaultFileName)) {
                currentDatabaseFile = file;
            }
        }
        if(currentDatabaseFile == null) {
            currentDatabaseFile = new File(dataDirectory, defaultFileName);
        }

        try {
            if (currentDatabaseFile.createNewFile()) {
                // file has yet to be created. Ensure it exists and has the bare minimum xml in it!
                initializeDatabase();
            }
        } catch (IOException e) {
            throw new RuntimeException("IO exception creating database file", e);
        }

        currentDatabaseDocument = readFileToDocument(currentDatabaseFile);
    }

    private static Document readFileToDocument(File file) {
        Document document;
        try {
            document = new SAXBuilder().build(file);
        } catch (JDOMException e) {
            throw new RuntimeException("Error reading main database file, errors in xml", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading main database file, there was an io exception", e);
        }
        return document;
    }

    private void initializeDatabase() {
        Document document = new Document();
        document.setRootElement(new Element(ROOT_ELEMENT_NAME));
        writeDocumentToFile(document);
    }

    public static void saveJob(Job toSave) {
        synchronized (instance) {
            instance.saveJobInternal(toSave);
        }
    }

    private void saveJobInternal(Job toAdd) {
        Element element = XmlSerializer.classToXml(toAdd);
        Element root = currentDatabaseDocument.getRootElement();
        Element jobsElement = root.getChild(JOBS_ELEMENT_NAME);
        if (jobsElement == null) {
            jobsElement = new Element(JOBS_ELEMENT_NAME);
            root.addContent(jobsElement);
        }
        jobsElement.addContent(element);
        writeDocumentToFile(currentDatabaseDocument);
    }

    public static void saveActiveJob(String jobIdToSave) {
        synchronized (instance) {
            instance.saveActiveJobInternal(jobIdToSave);
        }
    }

    private void saveActiveJobInternal(String jobIdToSave) {
        Element activeJobElement = currentDatabaseDocument.getRootElement().getChild(ACTIVE_JOB_ELEMENT_NAME);
        if (activeJobElement == null) {
            activeJobElement = new Element(ACTIVE_JOB_ELEMENT_NAME);
            currentDatabaseDocument.getRootElement().addContent(activeJobElement);
        }
        activeJobElement.setText(jobIdToSave);
        writeDocumentToFile(currentDatabaseDocument);
    }

    public static List<Job> getJobs() {
        synchronized (instance) {
            return instance.getJobsInternal();
        }
    }

    private List<Job> getJobsInternal() {
        List<Job> jobs = new ArrayList<>();
        Element jobsElement = currentDatabaseDocument.getRootElement().getChild(JOBS_ELEMENT_NAME);
        if (jobsElement == null) {
            return jobs;
        }
        for (Element rootChild : jobsElement.getChildren()) {
            try {
                XmlSerializable deserialized = XmlSerializer.classFromXml(rootChild);
                if (deserialized instanceof Job) {
                    jobs.add((Job)deserialized);
                } // otherwise we don't care.
            } catch (XmlSerializationException e) {
                throw new RuntimeException(e);
            }
        }
        return jobs;
    }

    public static @Nullable String getActiveJob() {
        synchronized (instance) {
            return instance.getActiveJobInternal();
        }
    }

    private @Nullable String getActiveJobInternal() {
        Element activeJobElement = currentDatabaseDocument.getRootElement().getChild(ACTIVE_JOB_ELEMENT_NAME);
        if (activeJobElement == null) {
            return null;
        }
        return activeJobElement.getText();
    }

    /**
     * Writes an arbitrary {@link Document} into the database file, replacing it completely.
     * @param document the document to write.
     */
    private void writeDocumentToFile(Document document) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(currentDatabaseFile), SystemUtils.getEncodingToAlwaysUse()));
            outputter.output(document, writer);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("database file "+currentDatabaseFile+" was not found (deleted?)");
        } catch (IOException e) {
            throw new RuntimeException("io exception during writing to database file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // nothing to be done
                }
            }
        }
    }
}
