package ca.quadrilateral.jobsmarts.data.mongodb;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bson.Document;

import com.mongodb.client.FindIterable;

import ca.quadrilateral.jobsmarts.api.User;
import ca.quadrilateral.jobsmarts.data.api.IUserDataService;

@ApplicationScoped
public class MongoUserDataService implements IUserDataService {
    
    @Inject
    private MongoContext mongoContext;

    @Override
    public Collection<User> getAllUsers() {
        
        final FindIterable<Document> userDocuments = mongoContext.getDatabase().getCollection("users").find();
        
        return StreamSupport
            .stream(userDocuments.spliterator(), false)
            .map(this::documentToUser)
            .collect(Collectors.toList());
    }

    private User documentToUser(final Document document) {
        return User.of(document.getString("name"), document.getString("email"));
    }
    
}
