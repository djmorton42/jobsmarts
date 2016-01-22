package ca.quadrilateral.jobsmarts.data.mongodb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.jobsmarts.api.Job;
import ca.quadrilateral.jobsmarts.api.JobDetails;
import ca.quadrilateral.jobsmarts.api.JobSummary;
import ca.quadrilateral.jobsmarts.data.api.IJobDataService;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class MongoJobDataService implements IJobDataService {
    private static final Logger logger = LoggerFactory.getLogger(MongoJobDataService.class); 
    
    private static final BasicDBObject JOB_DETAILS_UUID_INDEX_DEF = 
            new BasicDBObject().append("uuid", 1);
    
    private static final BasicDBObject JOB_SUMMARY_UUID_INDEX_DEF = 
            new BasicDBObject().append("uuid", 1);
    
    private static final BasicDBObject JOB_SUMMARY_URL_DATE_INDEX_DEF = 
            new BasicDBObject()
                .append("jobUrl", 1)
                .append("datePosted", 1);
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    @PostConstruct
    private void init() {
        logger.info("Establishing MongoDB connection");
        this.mongoClient = new MongoClient();
        this.database = this.mongoClient.getDatabase("jobsmarts");
        
        createJobSummaryIndexes();        
        createJobDetailsIndexes();
    }
    
    @PreDestroy
    private void cleanup() {
        logger.info("Closing MongoDB connection");
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public void saveNewJobs(final Collection<Job> jobs) {
        if (jobs.size() > 0) {
            insertJobSummaries(jobs);
            insertJobDetails(jobs);
        } else {
            logger.info("No new jobs to insert.");
        }
    }

    @Override
    public boolean isNew(final JobSummary jobSummary) {
        final BasicDBObject filter = 
                new BasicDBObject()
                    .append("jobUrl", jobSummary.getJobUrl())
                    .append("datePosted", convertLocalDateToDate(jobSummary.getDatePosted()));
        
        final FindIterable<Document> existingJob = 
                database
                    .getCollection("jobsummaries")
                    .find(filter)
                    .limit(1);
        
        final boolean jobIsNew = existingJob.first() == null;
        
        return jobIsNew;
    }
    
    private void insertJobSummaries(final Collection<Job> jobs) {
        final MongoCollection<Document> collection = database.getCollection("jobsummaries");
        collection.insertMany(
                jobs
                    .stream()
                    .map(this::convertJobSummaryToDocument)
                    .collect(Collectors.toList())
                );
    }
        
    private void insertJobDetails(final Collection<Job> jobs) {
        final MongoCollection<Document> collection = database.getCollection("jobdetails");
        collection.insertMany(
                jobs
                    .stream()
                    .map(this::convertJobDetailsToDocument)
                    .collect(Collectors.toList())
                );
    }
        
    private Document convertJobDetailsToDocument(final Job job) {
        final Document document = new Document();
        
        final JobDetails jobDetails = job.getJobDetails();
        
        document.put("description", jobDetails.getDescriptionHtml());
        document.put("uuid", job.getUUID().toString());
        document.put("Date Posted", convertLocalDateToDate(jobDetails.getDatePosted()));
        document.putAll(jobDetails.getFields());
        
        return document;
    }
    
    private Document convertJobSummaryToDocument(final Job job) {
        final Document document = new Document();
        
        final JobSummary jobSummary = job.getJobSummary();        
        
        document.append("uuid", job.getUUID().toString());
        document.append("companyName", jobSummary.getCompanyName());
        document.append("companyUrl", jobSummary.getCompanyUrl());
        document.append("datePosted", convertLocalDateToDate(jobSummary.getDatePosted()));
        document.append("jobTitle", jobSummary.getJobTitle());
        document.append("jobType", jobSummary.getJobType());
        document.append("jobUrl", jobSummary.getJobUrl());
        document.append("location", jobSummary.getLocation());
        
        return document;
    }
    
    private Date convertLocalDateToDate(final LocalDate date) {
        return new Date(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    private void createJobSummaryIndexes() {
        final MongoCollection<Document> collection = database.getCollection("jobsummaries");
        
        collection.createIndex(JOB_SUMMARY_UUID_INDEX_DEF);
        collection.createIndex(JOB_SUMMARY_URL_DATE_INDEX_DEF);
    }
    
    private void createJobDetailsIndexes() {
        final MongoCollection<Document> collection = database.getCollection("jobdetails");
        
        collection.createIndex(JOB_DETAILS_UUID_INDEX_DEF);
    }
}
