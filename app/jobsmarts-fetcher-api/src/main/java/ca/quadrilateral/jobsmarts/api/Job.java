package ca.quadrilateral.jobsmarts.api;

import java.time.LocalDateTime;
import java.util.UUID;

public class Job {
    private final JobSummary jobSummary;
    private final JobDetails jobDetails;
    private final UUID uuid;
    private final LocalDateTime retrievalDate;
    
    private Job(final JobSummary jobSummary, final JobDetails jobDetails) {
        this.jobSummary = jobSummary;
        this.jobDetails = jobDetails;
        this.uuid = UUID.randomUUID();
        this.retrievalDate = LocalDateTime.now();
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
        return uuid;
    }
    
    public LocalDateTime getRetrievalDate() {
        return retrievalDate;
    }
}
