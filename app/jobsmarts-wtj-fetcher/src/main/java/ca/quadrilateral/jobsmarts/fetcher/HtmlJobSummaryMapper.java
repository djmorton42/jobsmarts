package ca.quadrilateral.jobsmarts.fetcher;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ca.quadrilateral.jobsmarts.api.JobSummary;

public class HtmlJobSummaryMapper implements Function<Element, JobSummary> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d");
    private static final Pattern JOB_NAME_PATTERN = Pattern.compile("(.*?) ?(\\(.*?\\))?", Pattern.DOTALL);
    
    @Override
    public JobSummary apply(final Element element) {
        return JobSummary.of(
                extractDate(element),
                extractCompanyName(element),
                extractCompanyUrl(element),
                extractJobTitle(element),
                extractJobUrl(element),
                extractJobType(element),
                extractLocation(element));
    }
    
    private String extractCompanyName(final Element rowElement) {
        final Element jobCompanyElement = getJobSummaryTableData(rowElement, "wpjb-column-title");
        final Element spanElement = HtmlUtils.getSingleElement(jobCompanyElement.getElementsByTag("span"));
        
        final Matcher matcher = JOB_NAME_PATTERN.matcher(spanElement.text());
        
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("The specified element does not contain a parsable company name: " + rowElement);
        }
    }
    
    private String extractCompanyUrl(final Element rowElement) {
        final Element jobCompanyElement = getJobSummaryTableData(rowElement, "wpjb-column-title"); 
        
        final Elements anchorElements = jobCompanyElement.getElementsByTag("a");
        
        if (anchorElements.size() != 2) {
            return null;
        } else {
            return anchorElements.get(1).attr("href");            
        }
    }

    private String extractJobTitle(final Element rowElement) {
        final Element jobCompanyElement = getJobSummaryTableData(rowElement, "wpjb-column-title"); 
        
        final Elements anchorElements = jobCompanyElement.getElementsByTag("a");
        
        if (anchorElements.size() >= 1) {
            return anchorElements.get(0).text();
        } else {
            throw new IllegalArgumentException("The expected anchor elements were not present when extracting the job title");
        }
    }
    
    private String extractJobUrl(final Element rowElement) {
        final Element jobCompanyElement = getJobSummaryTableData(rowElement, "wpjb-column-title"); 
        
        final Elements anchorElements = jobCompanyElement.getElementsByTag("a");
        
        if (anchorElements.size() >= 1) {
            return anchorElements.get(0).attr("href");
        } else {
            throw new IllegalArgumentException("The expected anchor elements were not present when extracting the job url");   
        }
    }
    
    private String extractLocation(final Element rowElement) {
        return getJobSummaryTableData(rowElement, "wpjb-column-location").text();
    }
    
    
    private LocalDate extractDate(final Element rowElement) {
        final String dateText = getSanitizedDateElement(rowElement).text();
        
        if ("Today".equalsIgnoreCase(dateText)) {
            return LocalDate.now();
        } else {        
            final MonthDay monthDay = MonthDay.parse(dateText, FORMATTER);
            return LocalDate.now().with(monthDay);
        }
    }
    
    private Element getSanitizedDateElement(final Element rowElement) {
        
        final Element dateElementClone = getJobSummaryTableData(rowElement, "wpjb-column-date").clone();
        
        dateElementClone.children().stream().forEach(Element::remove);
        
        return dateElementClone;
    }

    private String extractJobType(final Element rowElement) {
        return getJobSummaryTableData(rowElement, "wpjb-column-type").text();
    }

    private Element getJobSummaryTableData(final Element rowElement, final String columnClassKey) {
        return HtmlUtils.getSingleElement(rowElement.getElementsByAttributeValueContaining("class", columnClassKey));
    }
}
