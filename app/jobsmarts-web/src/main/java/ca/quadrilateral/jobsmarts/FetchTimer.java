package ca.quadrilateral.jobsmarts;

import java.util.Collection;
import java.util.Date;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
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
    
    private static final int BASE_EXPIRY_PERIOD_MS = 20 * 60 * 1000;
    private static final int EXPIRY_MAX_VARIATION_MS = 30 * 60 * 1000;
    
    private static final Random random = new Random();
    
    @Resource
    private TimerService timerService;
    
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
    
    @Timeout 
    private void fetchJobs() {
        logger.info("Executing job fetch.");
        
        final Collection<Job> jobs = jobFetcher.fetchJobs();
        
        logger.info("{} new jobs found", jobs.size());
        
        jobDataService.saveNewJobs(jobs);
        jobs.forEach(jobEvent::fire);
        
        logger.info("Job fetch complete.");
        
        final Date nextTimeout = calculateNextTimeoutDate();
        
        logger.info("Next fetch will occur at " + nextTimeout);
        
        timerService.createSingleActionTimer(nextTimeout, new TimerConfig(null, false));
    }
    
    private Date calculateNextTimeoutDate() {
        final int variation = random.nextInt(EXPIRY_MAX_VARIATION_MS);
        final int nextTimeout = BASE_EXPIRY_PERIOD_MS + variation;
        
        return new Date(new Date().getTime() + nextTimeout);
    }
}
