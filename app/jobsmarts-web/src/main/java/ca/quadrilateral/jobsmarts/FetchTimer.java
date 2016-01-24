package ca.quadrilateral.jobsmarts;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.jobsmarts.api.IJobFetcher;
import ca.quadrilateral.jobsmarts.api.Job;
import ca.quadrilateral.jobsmarts.data.api.IJobDataService;

@Singleton
@Startup
public class FetchTimer {
    private static final Logger logger = LoggerFactory.getLogger(FetchTimer.class);
    
    @Inject
    private IJobDataService jobDataService;
    
    @Inject
    private IJobFetcher jobFetcher;

    @Inject
    private Event<Job> jobEvent;
    
    @PostConstruct
    private void init() {
        fetchJobs();
    }
    
    @Timeout @Schedule(minute="*/30", hour="*", persistent=false) 
    private void fetchJobs() {
        logger.info("Executing job fetch.");
        
        final Collection<Job> jobs = jobFetcher.fetchJobs();
        
        logger.info("{} new jobs found", jobs.size());
        
        jobDataService.saveNewJobs(jobs);
        jobs.forEach(jobEvent::fire);
        
        logger.info("Job fetch complete.");
    }
}
