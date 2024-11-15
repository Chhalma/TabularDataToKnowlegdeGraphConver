package coursework2.taskRDF;

import java.util.Set;

import org.apache.jena.rdf.model.Statement;


public class WikidataEndpoint extends SPARQLEndpointService{
	
	
	//https://www.mediawiki.org/wiki/Wikidata_Query_Service/User_Manual#SPARQL_endpoint	
	//One coudl also use the toolkit to access items. See if more efficient
	
	
			
	@Override
	public String getENDPOINT() {
		return "https://query.wikidata.org/sparql";
	}

	
	



	




	@Override
	protected String createSPARQLQuery_AllTypesForSubject(String uri_resource) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	protected String createSPARQLQuery_AllSuperClassesForSubject(String uri_resource) {
		// TODO Auto-generated method stub
		return null;
	}
		

	
	@Override
	protected String createSPARQLQueryForObject(String uri_object) {
		return //"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n "+
				"SELECT ?s ?p \n"
				+ "WHERE { ?s ?p <" + uri_object + "> . "
				+ "}";
	}
	
	
	
	protected String createSPARQLQueryForSubject(String uri_subject){
		
		return //"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n "+
				"SELECT ?p ?o \n"
				+ "WHERE { <" + uri_subject + "> ?p ?o . "
				+ "}";
		
	}
	
	
	/**
	 * To extract class types of the subject
	 * @param uri_subject
	 * @return
	 */	
	protected String createSPARQLQuery_TypesForSubject(String uri_subject){
		
		return //"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n "+
				"SELECT DISTINCT ?uri \n"
				+ "WHERE { <" + uri_subject + "> <http://www.wikidata.org/prop/direct/P31> ?uri . "
				+ "}";
		
	}
	
	@Override
	protected String craeteSPARQLQuery_TypeObjectsForPredicate(String uri_predicate) {
		return "SELECT DISTINCT ?uri \n"
				+ "WHERE { ?s <" + uri_predicate + "> ?o . "
				+ "?o <http://www.wikidata.org/prop/direct/P31> ?uri ."
				+ "}";
	}
	
	/*protected String createSPARQLQuery_LabelForSubject(String uri_subject){
		
		return //"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n "+
				"SELECT DISTINCT ?l \n"
				+ "WHERE { <" + uri_subject + "> <http://www.wikidata.org/prop/Q722218> ?l . "
				+ "}";
		
	}*/
	//TODO: query for labels!
	
	
	
	
	
	public static void main(String[] args) {
		
		
		String subject = "http://www.wikidata.org/entity/Q974";
		
		WikidataEndpoint wde =  new WikidataEndpoint();
		
		try {
			//System.out.println(wde.getValuesForQuery(wde.createSPARQLQuery_LabelForSubject(subject)));
			//System.out.println(wde.getTriplesForSubject(subject));
			System.out.println(wde.getLabelsForSubject(subject));
			//System.out.println(wde.getTypesForSubject(subject));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}




}
