package org.cyanogenmod.changelog.misc;

/**
 * Created by LuK on 2015-10-08.
 */
public class Change {
    public String mSubject;
    public String mProject;
    public String mLastUpdated;

    public Change(String subject, String project, String lastUpdated) {
        mSubject = subject;
        mProject = project;
        mLastUpdated = lastUpdated;
    }
}