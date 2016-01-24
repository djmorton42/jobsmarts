package ca.quadrilateral.jobsmarts.fetcher;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.quadrilateral.jobsmarts.api.IJobFetcher;
import ca.quadrilateral.jobsmarts.api.Job;
import ca.quadrilateral.jobsmarts.api.JobDetails;
import ca.quadrilateral.jobsmarts.api.JobSummary;
import ca.quadrilateral.jobsmarts.api.exception.JobFetchException;
import ca.quadrilateral.jobsmarts.api.exception.JobPageNotFoundException;
import ca.quadrilateral.jobsmarts.data.api.IJobDataService;

public class WaterlooTechJobsFetcher implements IJobFetcher {
    private static final Logger logger = LoggerFactory.getLogger(WaterlooTechJobsFetcher.class);
    
    private static final String JOB_LIST_URL = "https://www.waterlootechjobs.com/jobs/";
    private static final HtmlJobSummaryMapper HTML_TO_JOB_SUMMARY_FUNCTION = new HtmlJobSummaryMapper();
    private static final DateTimeFormatter JOB_DETAILS_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    
    @Inject
    private IJobDataService jobDataService;
    
    @Override
    public Collection<Job> fetchJobs() {
        return fetchJobsFromPage(1);
    }
        
    private Collection<Job> fetchJobsFromPage(final int pageNumber) {
        final Collection<Job> newJobs = new ArrayList<>();
        
        final String effectiveUrl = buildUrlString(pageNumber);

        try {
            final String jobListPageText = getHtmlPage(effectiveUrl);
    
            final Document jobListDocument = Jsoup.parse(jobListPageText);
            
            if (jobListDocument.getElementsByAttributeValueContaining("class", "wpjb-table-empty").size() > 0) {
                return newJobs;
            }
            
            final Element jobListElement = jobListDocument.getElementById("wpjb-job-list");
            final Elements tableRows = jobListElement.getElementsByTag("tr");
    
            final List<JobSummary> allJobSummaries = tableRows
                    .stream()
                    .filter(row -> !row.parent().tagName().equals("thead"))
                    .map(HTML_TO_JOB_SUMMARY_FUNCTION)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved {} jobs from page {}", allJobSummaries.size(), pageNumber);
            
            final List<JobSummary> newJobSummaries = allJobSummaries
                    .stream()
                    .filter(jobDataService::isNew)
                    .collect(Collectors.toList());
            
            logger.info("{} jobs from page {} are new", newJobSummaries.size(), pageNumber);
            
            newJobs.addAll(newJobSummaries
                    .stream()
                    .map(jobSummary -> Job.of(jobSummary, fetchJobDetails(jobSummary.getJobUrl())))
                    .filter(job -> job.getJobDetails() != null)
                    .collect(Collectors.toList())
                    );   
    
            if (allJobSummaries.size() == newJobSummaries.size()) {
                logger.info("All jobs on this page {} are new... trying next page", pageNumber);
                
                newJobs.addAll(fetchJobsFromPage(pageNumber + 1));
            }
        } catch (final JobFetchException e) {
            logger.warn("Exception fetching jobs", e);
            return newJobs;
        }
        
        return newJobs;
    }
    
    private String buildUrlString(final int pageNumber) {
        return JOB_LIST_URL + (pageNumber == 1 ? "" : "page/" + pageNumber);
    }
    
    private String getHtmlPage(final String url) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {                
                HttpEntity entity = response.getEntity();
                
                try (final InputStream inputStream = entity.getContent()) {
                    
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new JobPageNotFoundException("Job Page Not found for URL: " + url);
                    }
                    
                    final String charset = getContentCharset(response);
                    final String responseText = readStreamAsString(inputStream, charset);
    
                    return responseText;
                } finally {
                    if (entity != null) {
                        EntityUtils.consume(entity);
                    }
                }
            }
        } catch (final Exception e) {
            throw new JobFetchException(e);
        }
    }
    
    public JobDetails fetchJobDetails(final String url) {
        logger.debug("Fetching job details from URL: {}", url);
        try {
            final String jobPageText = getHtmlPage(url);
            
            final Document jobDocument = Jsoup.parse(jobPageText);
            
            final Elements fieldElements = jobDocument.getElementsByAttributeValueContaining("class", "wpjb-info-label");
            
            final JobDetails jobDetails = new JobDetails();
            
            for (Element fieldElement : fieldElements) {
                
                final Element firstSibling = HtmlUtils.getSingleElement(fieldElement.siblingElements()); 
                
                final Object fieldValue;
                final String fieldText = firstSibling.text();
                if (fieldElement.text().equals("Company Name:")) {
                    fieldValue = fieldText.replace(" (view profile)", "");
                } else if (fieldElement.text().equals("Date Posted")) {
                    final LocalDate datePosted;
                    if ("Today".equalsIgnoreCase(fieldText)) {
                        datePosted = LocalDate.now();
                    } else {
                        datePosted = LocalDate.parse(fieldText, JOB_DETAILS_DATE_FORMATTER);
                    }
                    jobDetails.setDatePosted(datePosted);
                    
                    continue;
                } else {
                    fieldValue = firstSibling.text();
                }
                
                jobDetails.addField(fieldElement.text(), fieldValue);    
            }
           
            final Element htmlDescriptionElement = jobDocument.getElementsByAttributeValueContaining("class", "wpjb-job-content").first();
            
            jobDetails.setDescriptionHtml(htmlDescriptionElement.toString());
            
            return jobDetails;
        } catch (final Exception e) {
            logger.warn("Job Details could not be loaded for URL: {}", url);
            return null;
        }
    }
    
    private String readStreamAsString(final InputStream inputStream, final String charset) throws Exception {
        try (final InputStreamReader inputStreamReader = charset != null ? new InputStreamReader(inputStream, charset) : new InputStreamReader(inputStream)) {
            final StringBuilder builder = new StringBuilder(8192);
            
            final char[] charBuffer = new char[4096];
            
            int charactersRead = 0;
            while (charactersRead != -1) {
                charactersRead = inputStreamReader.read(charBuffer);
            
                for (int i = 0; i < charactersRead; i++) {
                    builder.append(charBuffer[i]);
                }
            }
            
            return builder.toString();
        }
    }
    
    private String getContentCharset(final HttpResponse httpResponse) {
        return Arrays.asList(httpResponse.getHeaders("content-type"))
                .stream()
                .filter(contentTypeHeader -> "charset".equalsIgnoreCase(contentTypeHeader.getName()))
                .map(contentTypeHeader -> contentTypeHeader.getValue())
                .findFirst()
                .orElse(null);
    }   
}