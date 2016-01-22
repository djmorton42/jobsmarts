package ca.quadrilateral.jobsmarts.api;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class JobDetails {
    private LocalDate datePosted = null;
    private final Map<String, Object> fields = new HashMap<>();
    private String descriptionHtml;
    
    public JobDetails() {
    }

    public void setDescriptionHtml(final String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }
    
    public void setDatePosted(final LocalDate datePosted) {
        this.datePosted = datePosted;
    }
    
    public void addField(final String key, final Object value) {
        this.fields.put(key, value);
    }
    
    public Map<String, Object> getFields() {
        return fields;
    }
    
    public LocalDate getDatePosted() {
        return datePosted;
    }
    
    public String getDescriptionHtml() {
        return descriptionHtml;
    }
    
    
    
}
