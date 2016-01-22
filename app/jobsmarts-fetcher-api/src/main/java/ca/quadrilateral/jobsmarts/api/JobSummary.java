package ca.quadrilateral.jobsmarts.api;

import java.time.LocalDate;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class JobSummary {
    private final LocalDate datePosted;
    private final String companyName;
    private final String companyUrl;
    private final String jobTitle;
    private final String jobUrl;
    private final String jobType;
    private final String location;
    
    private JobSummary(
            final LocalDate datePosted,
            final String companyName,
            final String companyUrl,
            final String jobTitle,
            final String jobUrl,
            final String jobType,
            final String location
            ) {
        this.datePosted = datePosted;
        this.companyName = companyName;
        this.companyUrl = companyUrl;
        this.jobTitle = jobTitle;
        this.jobUrl = jobUrl;
        this.jobType = jobType;
        this.location = location;
    }
    
    public static JobSummary of(
            final LocalDate datePosted,
            final String companyName,
            final String companyUrl,
            final String jobTitle,
            final String jobUrl,
            final String jobType,
            final String location
            ) {
        return new JobSummary(datePosted, companyName, companyUrl, jobTitle, jobUrl, jobType, location);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("Job Title", jobTitle)
            .append("Job Type", jobType)
            .append("Company Name", companyName)
            .append("Location", location)
            .append("Date Posted", datePosted)
            .append("Job URL", jobUrl)
            .append("Company URL", companyUrl)
            .toString();
    }

    public LocalDate getDatePosted() {
        return datePosted;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobUrl() {
        return jobUrl;
    }
    
    public String getJobType() {
        return jobType;
    }
    
    public String getLocation() {
        return location;
    }
}
