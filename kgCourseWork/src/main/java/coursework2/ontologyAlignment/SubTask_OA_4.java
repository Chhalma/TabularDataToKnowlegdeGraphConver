package coursework2.ontologyAlignment;

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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import util.ReadFile;
import util.WriteFile;

public class SubTask_OA_4 {
	
	Model model;
	String reasoningFile;
	
	SubTask_OA_4(){
		
		 reasoningFile = "files/coursework2/solution/cw_pizza_equivalance_data_reasoning.ttl";
		 //reasoningFile = "files/coursework2/output_p/cw_pizza_equivalance_data_reasoning_p.ttl";
			
		   Dataset extendedDataset = RDFDataMgr.loadDataset(reasoningFile, RDFLanguages.TURTLE);
		   this.model = extendedDataset.getDefaultModel();
			
	}

	public void performSPARQLQuery(Model model, String file_query_out) throws FileNotFoundException {
	 	
		 
		  
		 	WriteFile writer = new WriteFile(file_query_out);
		 	
		     
		    String query_file = "files/coursework2/solution/query/query_OA_4.txt";
			ReadFile qfile = new ReadFile(query_file);		
			String queryStr = qfile.readFileIntoString();
					
		//	q = QueryFactory.create(queryStr);
			
		     Query q = QueryFactory.create(queryStr);
				
				QueryExecution qe =
						QueryExecutionFactory.create(q, model);
				try {
					ResultSet res = qe.execSelect();
						
					int solutions = 0;
						
					while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Menu Item,Ingredient");
						}
						solutions++;
						QuerySolution soln = res.next();
						Literal  menuItem = soln.getLiteral("?menuItem");
						Literal ing = soln.getLiteral("?ingLabel");
						//RDFNode ing = soln.get("?ing");
						
						writer.writeLine(menuItem.toString()+","+ing.toString());
					}
					System.out.println(solutions + " pizza satisfying the query.");
					    
				} finally {
					qe.close();
				}
				
				writer.closeBuffer();
		 
						
		 }



	
	public static void main(String[] args) {

		SubTask_OA_4 solution = new SubTask_OA_4();		    
		 		    
			//SPARQL results into CSV
			try {
				solution.performSPARQLQuery(solution.model, "files/coursework2/solution/query-result_OA_4.csv");				    
				
			//	solution.performSPARQLQuery(solution.model, "files/coursework2/output_p/query-result_OA_4.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				    
			
		
   	 		
	}

}
