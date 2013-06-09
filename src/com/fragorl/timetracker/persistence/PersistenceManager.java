package com.fragorl.timetracker.persistence;

import com.fragorl.timetracker.jobs.Job;
import com.fragorl.timetracker.serialization.XmlSerializable;
import com.fragorl.timetracker.serialization.XmlSerializationException;
import com.fragorl.timetracker.serialization.XmlSerializer;
import com.fragorl.timetracker.time.TimeSegment;
import com.fragorl.timetracker.time.TimeSerialization;
import com.fragorl.timetracker.util.SystemUtils;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

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
    private static final String JOBS_TIME_WORKED_ELEMENT_NAME = "jobsTimeWorked";
    private static final String JOB_TIME_WORKED_ELEMENT_NAME = "jobTimeWorked";
    private static final String TIME_SEGMENTS_ELEMENT_NAME = "timeSegments";
    private static final String JOB_ID_ELEMENT_NAME = "jobName";

    private static final PersistenceManager instance = new PersistenceManager();
    private static final TimeSerialization.StorageType DEFAULT_STORAGE_TYPE = TimeSerialization.StorageType.EpochMillis;

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
        Element jobsRoot = getOrCreateJobsRoot();
        jobsRoot.addContent(element);
        writeCurrentDatabase();
    }

    /**
     * Saves the current active job, or null
     * @param jobIdToSave the job to save, can be null to nullify, which will remove the active job element altogether.
     * @return true if the active job element was changed at all, false if it wasn't.
     */
    public static boolean saveActiveJob(@Nullable String jobIdToSave) {
        synchronized (instance) {
            return instance.saveActiveJobInternal(jobIdToSave);
        }
    }

    private boolean saveActiveJobInternal(@Nullable String jobIdToSave) {
        Element activeJobElement = currentDatabaseDocument.getRootElement().getChild(ACTIVE_JOB_ELEMENT_NAME);
        boolean didActiveJobElementExistInitially = activeJobElement != null;
        if (!didActiveJobElementExistInitially) {
            activeJobElement = new Element(ACTIVE_JOB_ELEMENT_NAME);
            currentDatabaseDocument.getRootElement().addContent(activeJobElement);
        }
        if (jobIdToSave == null) {
            currentDatabaseDocument.getRootElement().removeContent(activeJobElement);
            writeCurrentDatabase();
            return didActiveJobElementExistInitially;
        } else {
            String oldText = activeJobElement.getText();
            activeJobElement.setText(jobIdToSave);
            writeCurrentDatabase();
            return !oldText.equals(jobIdToSave);
        }
    }

    public static List<Job> getJobs() {
        synchronized (instance) {
            return new ArrayList<>(instance.getJobsInternal().values());
        }
    }

    private Map<Element, Job> getJobsInternal() {
        ImmutableMap.Builder<Element, Job> builder = ImmutableMap.builder();
        Element jobsRoot = currentDatabaseDocument.getRootElement().getChild(JOBS_ELEMENT_NAME);
        if (jobsRoot == null) {
            return builder.build();
        }
        for (Element rootChild : jobsRoot.getChildren()) {
            try {
                XmlSerializable deserialized = XmlSerializer.classFromXml(rootChild);
                if (deserialized instanceof Job) {
                    builder.put(rootChild, (Job) deserialized);
                } // otherwise we don't care.
            } catch (XmlSerializationException e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
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

    public static @Nullable List<TimeSegment> getTimeWorkedSegments(String jobId) {
        synchronized (instance) {
            return instance.getTimeWorkedSegmentsInternal(jobId);
        }
    }

    private @Nullable List<TimeSegment> getTimeWorkedSegmentsInternal(String jobId) {
        Element timeWorkedRoot = getOrCreateTimeWorkedRoot();
        Element jobTimeWorkedRoot = getJobTimeWorkedRootById(timeWorkedRoot, jobId);
        if (jobTimeWorkedRoot == null) {
            return null;
        }
        List<Element> timeSegmentElement = jobTimeWorkedRoot.getChild(TIME_SEGMENTS_ELEMENT_NAME).getChildren();
        List<TimeSegment> result = new ArrayList<>();
        for (Element childElement : timeSegmentElement) {
            try {
                TimeSegment timeSegment = TimeSerialization.deserialize(childElement);
                result.add(timeSegment);
            } catch (XmlSerializationException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static void saveTimeWorkedSegment(String jobId, TimeSegment segment) {
        synchronized (instance) {
            instance.saveTimeWorkedSegmentInternal(jobId, segment);
        }
    }

    private void saveTimeWorkedSegmentInternal(String jobId, TimeSegment segment) {
        Element allJobsTimeWorkedRoot = getOrCreateTimeWorkedRoot();
        Element jobTimeWorkedRoot = getJobTimeWorkedRootById(allJobsTimeWorkedRoot, jobId);
        if (jobTimeWorkedRoot == null) {
            jobTimeWorkedRoot = createEmptyJobTimeWorkedRoot(jobId);
            allJobsTimeWorkedRoot.addContent(jobTimeWorkedRoot);
        }
        Element timeSegmentsRoot = jobTimeWorkedRoot.getChild(TIME_SEGMENTS_ELEMENT_NAME);
        timeSegmentsRoot.addContent(TimeSerialization.serialize(segment, DEFAULT_STORAGE_TYPE));
        writeCurrentDatabase();
    }

    private static @Nullable Element getJobTimeWorkedRootById(Element allJobsTimeWorkedRoot, String jobId) {
        for (Element jobTimeWorkedRoot : allJobsTimeWorkedRoot.getChildren()) {
            String idOrNull = jobTimeWorkedRoot.getChildText(JOB_ID_ELEMENT_NAME);
            if (idOrNull != null && idOrNull.equals(jobId)) {
                return jobTimeWorkedRoot;
            }
        }
        return null;
    }

    private static Element createEmptyJobTimeWorkedRoot(String jobId) {
        Element result = new Element(JOB_TIME_WORKED_ELEMENT_NAME);
        result.addContent(new Element(JOB_ID_ELEMENT_NAME).setText(jobId));
        result.addContent(new Element(TIME_SEGMENTS_ELEMENT_NAME));
        return result;
    }

    /**
     * Attempts to delete a job with the given id, returning true if it existed (and was deleted), or false if it didn't (and wasn't)
     * @param jobId the id of the job
     * @return true a job by the id of jobId existed (and was deleted), or false if it didn't (and wasn't)
     */
    public static boolean deleteJob(String jobId) {
        synchronized (instance) {
            return instance.deleteJobInternal(jobId);
        }
    }

    private boolean deleteJobInternal(String jobId) {
        Map<Element, Job> elementsToJobs = getJobsInternal();
        for (Map.Entry<Element, Job> elementToJob : elementsToJobs.entrySet()) {
            if (elementToJob.getValue().getId().equals(jobId)) {
                // boom, we found it.
                Element jobsRoot = getOrCreateJobsRoot();
                jobsRoot.removeContent(elementToJob.getKey()); // remove it from the active document
                Element timeWorkedRoot = getOrCreateTimeWorkedRoot();
                @Nullable Element timeWorkedElementForJob = getJobTimeWorkedRootById(timeWorkedRoot, jobId);
                if (timeWorkedElementForJob != null) {
                    timeWorkedRoot.removeContent(timeWorkedElementForJob);
                }
                writeCurrentDatabase(); // only here do we need to save.
                return true;
            }
        }
        return false;
    }

    private Element getOrCreateJobsRoot() {
        Element rootElement = currentDatabaseDocument.getRootElement();
        Element jobsRoot = rootElement.getChild(JOBS_ELEMENT_NAME);
        if (jobsRoot == null) {
            jobsRoot = new Element(JOBS_ELEMENT_NAME);
            rootElement.addContent(jobsRoot);
        }
        return jobsRoot;
    }

    private Element getOrCreateTimeWorkedRoot() {
        Element rootElement = currentDatabaseDocument.getRootElement();
        Element timeWorkedRoot = rootElement.getChild(JOBS_TIME_WORKED_ELEMENT_NAME);
        if (timeWorkedRoot == null) {
            rootElement.addContent(timeWorkedRoot = new Element(JOBS_TIME_WORKED_ELEMENT_NAME));
        }
        return timeWorkedRoot;
    }

    private void writeCurrentDatabase() {
        writeDocumentToFile(currentDatabaseDocument);
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
