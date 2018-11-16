package com.latidude99.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.latidude99.model.Enquiry;

@Transactional
@Service
public class HibernateSearchService {
	
	@Autowired
	EnquiryService enquiryService;

	@Autowired
	private EntityManager entityManager;
	

    public List<Enquiry> hibernateSearch(String selector, String searchFor, int limit, String dateRangeString) {  
    	
    	FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager); 
    	QueryBuilder queryBuilder = fullTextEntityManager 
    	        		.getSearchFactory()
    	        		.buildQueryBuilder()
    	        		.forEntity(Enquiry.class)
    	        		.get();
    	
    	Query luceneQuery;
        switch(selector) {
        	case "keywordExact":
        		luceneQuery = keywordExactQuery(searchFor, queryBuilder);
        		System.out.println("Hibernate search, keywordExact -> " + searchFor);
        		break;
        	case "keywordFuzzy1":
        		luceneQuery = keywordFuzzy1Query(searchFor, queryBuilder);
        		System.out.println("Hibernate search, keywordFuzzy1 -> " + searchFor);
				break;
        	case "keywordFuzzy2":
        		luceneQuery = keywordFuzzy2Query(searchFor, queryBuilder);
        		System.out.println("Hibernate search, keywordFuzzy2 -> " + searchFor);
				break;
        	case "keywordWildcard":
        		luceneQuery = keywordWildcardQuery(searchFor, queryBuilder);
        		System.out.println("Hibernate search, keywordWildcard -> " + searchFor);
				break;
        	case "phraseExact":
        		luceneQuery = phraseExactQuery(searchFor, queryBuilder);
        		System.out.println("Hibernate search, phraseExact -> " + searchFor);
				break;
        	case "phraseSlop1":
        		luceneQuery = phraseSlop1Query(searchFor, queryBuilder);
        		System.out.println("Hibernate search, phraseSlop1 -> " + searchFor);
				break;
        	case "phraseSlop2":
        		luceneQuery = phraseSlop2Query(searchFor, queryBuilder);
        		System.out.println("Hibernate search, phraseSlop2 -> " + searchFor);
				break;
        	case "phraseSlop3":
        		luceneQuery = phraseSlop3Query(searchFor, queryBuilder);
        		System.out.println("Hibernate search, phraseSlop3 -> " + searchFor);
				break;
        	case "simpleQueryString":
        		luceneQuery = simpleQueryStringQuery(searchFor, queryBuilder);
        		System.out.println("Hibernate search, simpleQueryString -> " + searchFor);
				break;
			default:	
				luceneQuery = defaultQuery(searchFor, queryBuilder);
				System.out.println("Hibernate search, default -> " + searchFor);
        }
       
        
        // wrap Lucene query in an Hibernate Query object
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Enquiry.class);
        
        // execute search and return results (sorted by relevance as default)
        @SuppressWarnings("unchecked") 
        List<Enquiry> enquiryList = jpaQuery.getResultList();
        
      // limiting the results by date range
		String[] dateRange;
		if("".equals(dateRangeString) || dateRangeString == null) {
			dateRangeString = "01/01/1500 - 01/01/3000";
			dateRange = dateRangeString.trim().split("-");
		} else 
			dateRange = dateRangeString.trim().split("-");
		ZonedDateTime startDate = enquiryService.stringToZonedDateTimeConverter(dateRange[0].trim());
		ZonedDateTime endDate = enquiryService.stringToZonedDateTimeConverter(dateRange[1].trim());
		System.out.println("startDate: " + startDate + ", endDate: " + endDate);
        enquiryList = enquiryList.stream()
        						.filter(e -> e.getCreatedDate().isAfter(startDate))
        						.filter(e -> e.getCreatedDate().isBefore(endDate))
        						.collect(Collectors.toList());
    
     // limiting the results number
        if(limit < 1)  
			limit = Integer.MAX_VALUE;
        enquiryList = enquiryList.stream() 
								.limit(limit)
								.collect(Collectors.toList());   
        
        List<Enquiry> enquiryListWithProgressUsers = enquiryService.sortProgressUsers(enquiryList);
        return enquiryListWithProgressUsers;


    }
 
    //------------------building specific queries for the switch cases  (second step)-----------------------------
    
    public Query defaultQuery(String searchFor, QueryBuilder queryBuilder) {  
    	Query luceneQuery = queryBuilder
									.keyword()
									.fuzzy()
									  .withEditDistanceUpTo(1)
									  .withPrefixLength(0)
					        	    .onFields("name", "email", "message", "phone", "isbn")
					        	    .matching(searchFor)
					        	    .createQuery();
    	return luceneQuery;
    }
    
    public Query keywordExactQuery(String searchFor, QueryBuilder queryBuilder) {  
    	Query luceneQuery = queryBuilder
									.keyword()
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .matching(searchFor)
					        	    .createQuery();
    	return luceneQuery;
    }
    
    public Query keywordFuzzy1Query(String searchFor, QueryBuilder queryBuilder) {  
		Query luceneQuery = queryBuilder
									.keyword()
									.fuzzy()
									  .withEditDistanceUpTo(1)
									  .withPrefixLength(0)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .matching(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query keywordFuzzy2Query(String searchFor, QueryBuilder queryBuilder) {  
		Query luceneQuery = queryBuilder
									.keyword()
									.fuzzy()
									  .withEditDistanceUpTo(2)
									  .withPrefixLength(0)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .matching(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query keywordWildcardQuery(String searchFor, QueryBuilder queryBuilder) {  
		Query luceneQuery = queryBuilder
									.keyword()
									.wildcard()
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .matching(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query phraseExactQuery(String searchFor, QueryBuilder queryBuilder) { 
		Query luceneQuery = queryBuilder
									.phrase()
									.withSlop(0)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .sentence(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query phraseSlop1Query(String searchFor, QueryBuilder queryBuilder) { 
		Query luceneQuery = queryBuilder
									.phrase()
									.withSlop(1)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .sentence(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query phraseSlop2Query(String searchFor, QueryBuilder queryBuilder) { 
		Query luceneQuery = queryBuilder
									.phrase()
									.withSlop(2)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .sentence(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query phraseSlop3Query(String searchFor, QueryBuilder queryBuilder) { 
		Query luceneQuery = queryBuilder
									.phrase()
									.withSlop(3)
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .sentence(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    public Query simpleQueryStringQuery(String searchFor, QueryBuilder queryBuilder) { 
		Query luceneQuery = queryBuilder
									.simpleQueryString()
					        	    .onField("name")
					        	    	.boostedTo(8f)
					        	    .andField("email")
					        	        .boostedTo(6f)
					        	    .andField("message")
					        	    	.boostedTo(4f)
					        	    .andField("phone")
					        	    	.boostedTo(2f)
					        	    .andField("isbn")
					        	    	.boostedTo(4f)
					        	    .matching(searchFor)
					        	    .createQuery();
		return luceneQuery;
    }
    
    
    
    
}

















