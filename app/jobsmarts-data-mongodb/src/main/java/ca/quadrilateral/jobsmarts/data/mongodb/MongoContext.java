package ca.quadrilateral.jobsmarts.data.mongodb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class MongoContext {
    private static final Logger logger = LoggerFactory.getLogger(MongoContext.class);
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    @PostConstruct
    private void init() {
        logger.info("Establishing MongoDB connection");
        this.mongoClient = new MongoClient();
        this.database = this.mongoClient.getDatabase("jobsmarts");
    }
    
    @PreDestroy
    private void cleanup() {
        logger.info("Closing MongoDB connection");
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }
    
}
