package ca.quadrilateral.jobsmarts.data.api;

import java.util.Collection;

import ca.quadrilateral.jobsmarts.api.Job;
import ca.quadrilateral.jobsmarts.api.JobSummary;

public interface IJobDataService {
    void saveNewJobs(Collection<Job> jobs);
    boolean isNew(JobSummary jobSummary);
}
