package coursework2.reasoningSPARQL;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import util.ReadFile;
import util.WriteFile;

public class SPARQLTest {
	
	Model model;
	String reasoningFile;
	
	SPARQLTest(){
		
		 String reasoningFile = "files/coursework2/solution/IN3067-INM713_coursework_data_pizza_500-taskRDF-reasoning.ttl";
		//   String reasoningFile = "files/coursework2/output_p/result_with_ontology-reasoning.ttl";
			
		   Dataset extendedDataset = RDFDataMgr.loadDataset(reasoningFile, RDFLanguages.TURTLE);
		   this.model = extendedDataset.getDefaultModel();
			
	}

	public void performSPARQLQuery(Model model, String file_query_out) throws FileNotFoundException {
	 	
		 
		  
		 	String query_file = "files/coursework2/solution/query/test.txt";
			
			ReadFile qfile = new ReadFile(query_file);		
			String queryStr = qfile.readFileIntoString();
					
		//	q = QueryFactory.create(queryStr);
			
		     Query q = QueryFactory.create(queryStr);
				
				QueryExecution qe =
						QueryExecutionFactory.create(q, model);
				try {
					ResultSet res = qe.execSelect();
						
					int solutions = 0;
					System.out.println("Hello  ...");
					
					while( res.hasNext()) {
						solutions++;
						QuerySolution soln = res.next();
						System.out.println(soln.toString());
					}
					System.out.println(solutions + " pizza satisfying the query.");
					
				} finally {
					qe.close();
				}
							
		 }



	
	public static void main(String[] args) {

		SPARQLTest solution = new SPARQLTest();		    
		 		    
			//SPARQL results into CSV
			try {
				solution.performSPARQLQuery(solution.model, "files/coursework2/solution/SPARQL-query-result_test.csv");				    
				
				//solution.performSPARQLQuery(solution.model, "files/coursework2/output_p/query-result1.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				    
			
		
   	 		
	}

}

