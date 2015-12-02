package org.cyanogenmod.changelog.misc;

public class Change {
    public String mSubject;
    public String mProject;
    public String mLastUpdated;
    public String mId;

    public Change(String subject, String project, String lastUpdated, String id) {
        mSubject = subject;
        mProject = project;
        mLastUpdated = lastUpdated;
        mId = id;
    }
}