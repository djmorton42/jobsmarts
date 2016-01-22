package ca.quadrilateral.jobsmarts.api;

import java.util.UUID;

public class Job {
    private final JobSummary jobSummary;
    private final JobDetails jobDetails;
    private final UUID uuid;
    
    private Job(final JobSummary jobSummary, final JobDetails jobDetails) {
        this.jobSummary = jobSummary;
        this.jobDetails = jobDetails;
        this.uuid = UUID.randomUUID();
    }
    
    public static Job of(final JobSummary jobSummary, final JobDetails jobDetails) {
        return new Job(jobSummary, jobDetails);
    }

    public JobSummary getJobSummary() {
        return jobSummary;
    }

    public JobDetails getJobDetails() {
        return jobDetails;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
}
