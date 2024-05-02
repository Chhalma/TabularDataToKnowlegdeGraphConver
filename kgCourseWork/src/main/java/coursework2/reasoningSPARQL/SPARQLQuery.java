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

public class SPARQLQuery {
	
	Model model;
	String reasoningFile;
	
	SPARQLQuery(){
		
		 String reasoningFile = "files/coursework2/solution/IN3067-INM713_coursework_data_pizza_500-taskRDF-reasoning.ttl";
		//   String reasoningFile = "files/coursework2/output_p/result_with_ontology-reasoning.ttl";
			
		   Dataset extendedDataset = RDFDataMgr.loadDataset(reasoningFile, RDFLanguages.TURTLE);
		   this.model = extendedDataset.getDefaultModel();
			
	}

	public void performSPARQLQuery(Model model, String file_query_out) throws FileNotFoundException {
	 	
		 
		  
		 	WriteFile writer = new WriteFile(file_query_out);
		 	
		     
		    String query_file = "files/coursework2/solution/query/query1.txt";
		  // String query_file = "files/coursework2/solution/query/query2.txt";
		  // String query_file = "files/coursework2/solution/query/query3.txt";
		   // String query_file = "files/coursework2/solution/query/query4.txt";
		 //   String query_file = "files/coursework2/solution/query/query5.txt";
			
			ReadFile qfile = new ReadFile(query_file);		
			String queryStr = qfile.readFileIntoString();
					
		//	q = QueryFactory.create(queryStr);
			
		     Query q = QueryFactory.create(queryStr);
				
				QueryExecution qe =
						QueryExecutionFactory.create(q, model);
				try {
					ResultSet res = qe.execSelect();
						
					int solutions = 0;
			// Data Process for Query 1..........................			
				while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Restaurant Name, Menu Item,Price");//+","+String.valueOf(discountedPrice));//+","+population.toString()+",");
							
						}
						solutions++;
						QuerySolution soln = res.next();
						//RDFNode city = soln.get("?city");
						Literal  resName = soln.getLiteral("?restName");
						Literal  itemLit = soln.getLiteral("?menuItem");
						Literal priceLit = soln.getLiteral("?price");
						
						//RDFNode avgRes = soln.get("?avgRestaurant");
						
						double price = priceLit.getDouble();
						
						writer.writeLine(resName.toString()+","+itemLit.toString()+","+String.valueOf(price));//+","+String.valueOf(discountedPrice));//+","+population.toString()+",");
						
					}
					//Data Process for Query 2..................
				/*	while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Restaurant Name, Menu Item,Price, Discounted Price");//+","+String.valueOf(discountedPrice));//+","+population.toString()+",");
							
						}
						solutions++;
						QuerySolution soln = res.next();
						//RDFNode city = soln.get("?city");
						Literal  resName = soln.getLiteral("?restName");
						Literal  itemLit = soln.getLiteral("?itemName");
						Literal priceLit = soln.getLiteral("?price");
						Literal discountedPriceLiteral = soln.getLiteral("?discountedPrice");
						
						double price = priceLit.getDouble();
						double discountedPrice = discountedPriceLiteral.getDouble();
												
						writer.writeLine(resName.toString()+","+itemLit.toString()+","+String.valueOf(price)+","+String.valueOf(discountedPrice));
						
					}*/
					//Data Process for Query 3..................
				/*	while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Restaurant Name, Menu Item");
						}
						solutions++;
						QuerySolution soln = res.next();
						//RDFNode city = soln.get("?city");
						Literal  resName = soln.getLiteral("?restName");
						Literal  itemLit = soln.getLiteral("?itemName");
												
						writer.writeLine(resName.toString()+","+itemLit.toString());
						
					}*/
					//Data Process for Query 4..................
				/*  while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Menu Item, Avg Price");//+","+String.valueOf(discountedPrice));//+","+population.toString()+",");
							
						}
						solutions++;
						QuerySolution soln = res.next();
						
						Literal  itemLit = soln.getLiteral("?itemName");
						Literal avgPriceLit = soln.getLiteral("?averagePrice");
						double avgPrice = avgPriceLit.getDouble();
						
												
						writer.writeLine(itemLit.toString()+","+String.valueOf(avgPrice));
						
					}*/
					//Data Process for Query 5..................
				/*	while( res.hasNext()) {
						if(solutions == 0) {
							writer.writeLine("Restaurant Name, Count Menu Item,Min Price,Max Price, Avg Price");//+","+String.valueOf(discountedPrice));//+","+population.toString()+",");
							
						}
						solutions++;
						QuerySolution soln = res.next();
						
						Literal  itemLit = soln.getLiteral("?restaurantName");
						
						Literal  countMenuItemLit = soln.getLiteral("?countMenuItem");
						
						Literal minPriceLit = soln.getLiteral("?minPrice");
						Literal maxPriceLit = soln.getLiteral("?maxPrice");
						Literal avgPriceLit = soln.getLiteral("?avgPrice");
						
						int countMenuItem = countMenuItemLit.getInt();
						
						double minPrice = minPriceLit.getDouble();
						double maxPrice = maxPriceLit.getDouble();
						double avgPrice = avgPriceLit.getDouble();
						
												
						writer.writeLine(itemLit.toString()+","+String.valueOf(countMenuItem)+","+String.valueOf(minPrice)+","+String.valueOf(maxPrice)+","+String.valueOf(avgPrice));
						
					}*/
		
		
		
		
					System.out.println(solutions + " pizza satisfying the query.");
					    
				} finally {
					qe.close();
				}
				
				writer.closeBuffer();
		 
						
		 }



	
	public static void main(String[] args) {

			SPARQLQuery solution = new SPARQLQuery();		    
		 		    
			//SPARQL results into CSV
			try {
				solution.performSPARQLQuery(solution.model, "files/coursework2/solution/SPARQL-query-result1.csv");				    
				
				//solution.performSPARQLQuery(solution.model, "files/coursework2/output_p/query-result1.csv");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				    
			
		
   	 		
	}

}
