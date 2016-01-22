package ca.quadrilateral.jobsmarts.api;

import java.util.Collection;

public interface IJobFetcher {
    Collection<Job> fetchJobs();
}
