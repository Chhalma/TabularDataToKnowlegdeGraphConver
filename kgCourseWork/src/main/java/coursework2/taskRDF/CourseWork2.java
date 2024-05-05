package coursework2.taskRDF;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.CSVReader;

public class CourseWork2 {
	
	String input_file;
	Model model;
	InfModel inf_model;
	
	String coursework2,rdfs;
	//wheater use external lookup service for URI 
	boolean lookupExternalURI  = false;
	
	DBpediaLookup dbpedia;
	
	GoogleKGLookup googleKGLookup; //  instance of GoogleKGLookup
	
	WikidataLookup wikiKGLookup;

    
	List<String[]> csv_file;
	
	// These lists will collect vocabularies from provided ontology........
	
	List<String> restaurantTypeList ;
	List<String> menuItemTypeList;
	List<String> ingredientTypeList;
	List<String> objectPropertyList;
	List<String> dataPropertyList;
	
	//Dictionary that keeps the URIs. Specially useful if accessing a remote service to get a candidate URI  to avoid repeated calls
    Map<String, String> stringToURI = new HashMap<String, String>();
    
    Map<String, Integer> column_index;
    
    I_Sub isub = new I_Sub();
	
    public CourseWork2(String input_file, Map<String, Integer> column_index) throws IOException {
    	
    	this.input_file = input_file;
		//Useful to acces column by name (there are alternative ways to do so)
		this.column_index = column_index;
		
		restaurantTypeList = new ArrayList<>();
		menuItemTypeList = new ArrayList<>();
		ingredientTypeList = new ArrayList<>();
		objectPropertyList = new ArrayList<>();
		dataPropertyList = new ArrayList<>();
		
		//Load the ontology in the Model to retrieve all the vocabularies from the given Ontology....
		
    	String ontologyFile = "files/coursework2/pizza-restaurants-ontology.ttl";
		Dataset dataset = RDFDataMgr.loadDataset(ontologyFile, RDFLanguages.TURTLE);
		model = dataset.getDefaultModel();
		
		coursework2= "http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#";
		rdfs = "http://www.w3.org/2000/01/rdf-schema#";
		
		//Method call to doad the ontology...
	    loadOntologyData();
   
	    //Load data in matric to later use an iterator		
		loadCSVData();
    
            
    //KG
	    googleKGLookup = new GoogleKGLookup(); // Initialize GoogleKGLookup
	    
	    wikiKGLookup = new WikidataLookup();
	    
    //wikiLookup = new 
	
}
    private void loadOntologyData() {
		
    	addToRestaurantTypeList();
        addToMenuItemTypeList();
        addToIngredientTypeList();
        addToObjectPropertyList();
		addToDataPropertyList();
		
		System.out.println(restaurantTypeList);
		System.out.println(menuItemTypeList);
		System.out.println(ingredientTypeList);
		System.out.println(objectPropertyList);
		System.out.println(dataPropertyList);
		

	}
	private void loadCSVData() throws IOException {
        CSVReader reader = new CSVReader(new FileReader(input_file));
        csv_file = reader.readAll();
        reader.close();
    }
	private void addToRestaurantTypeList() {
        
	    String queryStr =
	    		 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    		 "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
	    	    		 "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"+
	    	    		 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	    	    		 "PREFIX cw: <http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#> "+
	    	    			                                            
	    	    			        "SELECT ?resLabel "+ 
	    	    			        "   WHERE {"+
	    	    			        "    {  ?subclass rdfs:subClassOf cw:Restaurant ."+
	    	    				    
	    	    			        "}"+
	    	    			        "UNION {"+
	    	    			        " ?superclass rdfs:subClassOf cw:Restaurant ."+
	    	    			        " ?subclass rdfs:subClassOf ?superclass . }"+
	    	    			        "?subclass rdfs:label ?resLabel ."+
	    	    			        "  }";    
	    					    // Create the query
	    	    			  Query   q = QueryFactory.create(queryStr);
	    	    			    // Execute the query on the model
	    	    			  QueryExecution qu = QueryExecutionFactory.create(q, model);
	    	    			       
	    	    			    // Execute the query and obtain the results
	    	    			    try  {
	    	    			        ResultSet results = qu.execSelect();
	    	    			        while (results.hasNext()) {
	    	    			            QuerySolution soln = results.next();
	    	    			            RDFNode resLabelNode = soln.get("resLabel");
	    	    			            String resLabelString = resLabelNode.toString();
    	    			                
	    	    			            restaurantTypeList.add(resLabelString.toLowerCase());
   	    			                  }
	    	    			        } finally {
	    	    			        	qu.close();
	    }
	}
	private void addToMenuItemTypeList() {
		String queryStr = 
		 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		 "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
	    		 "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"+
	    		 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	    		 "PREFIX cw: <http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#> "+
	    			                                            
	    			        "SELECT ?menuItemType "+ 
	    			        "   WHERE {"+
	    			        "    {  ?menuItem rdfs:subClassOf cw:MenuItem ."+
	    				    
	    			        "}"+
	    			        "UNION {"+
	    			        " ?superMenuItem rdfs:subClassOf cw:MenuItem ."+
	    			        " ?menuItem rdfs:subClassOf ?superMenuItem . }"+
	    			        "UNION {"+
	    			        " ?superMenuItem rdfs:subClassOf cw:MenuItem ."+
	    			        " ?menuItem1 rdfs:subClassOf ?superMenuItem . "+
	    			        " ?menuItem rdfs:subClassOf ?menuItem1 . }"+
	    			        "UNION {"+
	    			        " ?superMenuItem rdfs:subClassOf cw:MenuItem ."+
	    			        " ?menuItem1 rdfs:subClassOf ?superMenuItem . "+
	    			        " ?menuItem2 rdfs:subClassOf ?menuItem1 . "+
	    			        " ?menuItem rdfs:subClassOf ?menuItem2 .} "+
	    			        
	    			        "?menuItem rdfs:label ?menuItemType ."+
	    			        "  }";    
					    // Create the query
	    			  Query   q = QueryFactory.create(queryStr);
	    			    // Execute the query on the model
	    			  QueryExecution qu = QueryExecutionFactory.create(q, model);
	    			       
	    			    // Execute the query and obtain the results
	    			    try  {
	    			        ResultSet results = qu.execSelect();
	    			        while (results.hasNext()) {
	    			            QuerySolution soln = results.next();
	    			            RDFNode itemNode = soln.get("menuItemType");
	    			            if(itemNode != null) {
	    			            String itemTypeName = itemNode.toString();
	    			            menuItemTypeList.add(itemTypeName.toLowerCase());
	    			            }
	    			           
	    			        }
	    			    } finally {
	    			        qu.close();
	    			    }
	    		
	}
		private void addToIngredientTypeList() {
		String queryStr =
				
	    		 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		 "PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
	    		 "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"+
	    		 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	    		 "PREFIX cw: <http://www.semanticweb.org/city/in3067-inm713/2024/restaurants#> "+
	    			                                            
	    			        "SELECT ?ingredient "+ 
	    			        "   WHERE {"+
	    			        "    {  ?ingredient rdfs:subClassOf cw:Ingredient ."+
	    				    
	    			        "}"+
	    			        "UNION {"+
	    			        " ?superIngr rdfs:subClassOf cw:Ingredient ."+
	    			        " ?ingredient rdfs:subClassOf ?superIngr . }"+
	    			        "UNION {"+
	    			        " ?superIngr rdfs:subClassOf cw:Ingredient ."+
	    			        " ?ingredient1 rdfs:subClassOf ?superIngr . "+
	    			        " ?ingredient rdfs:subClassOf ?ingredient1 . }"+
	    			        "UNION {"+
	    			        " ?superIngr rdfs:subClassOf cw:Ingredient ."+
	    			        " ?ingredient1 rdfs:subClassOf ?superIngr . "+
	    			        " ?ingredient2 rdfs:subClassOf ?ingredient1 . "+
	    			        " ?ingredient rdfs:subClassOf ?ingredient2 .} "+
	    			        "UNION {"+
	    			        " ?superIngr rdfs:subClassOf cw:Ingredient ."+
	    			        " ?ingredient1 rdfs:subClassOf ?superIngr . "+
	    			        " ?ingredient2 rdfs:subClassOf ?ingredient1 . "+
	    			        " ?ingredient3 rdfs:subClassOf ?ingredient2 . "+
	    			        " ?ingredient rdfs:subClassOf ?ingredient3 .} "+
	    			        
	    			       // "?ingredient rdfs:label ?ingredientName "+
	    			        "  }";    
					    // Create the query
	    			  Query   q = QueryFactory.create(queryStr);
	    			    // Execute the query on the model
	    			  QueryExecution qu = QueryExecutionFactory.create(q, model);
	    			       
	    			    // Execute the query and obtain the results
	    			    try  {
	    			        ResultSet results = qu.execSelect();
	    			        while (results.hasNext()) {
	    			            QuerySolution soln = results.next();
	    			            RDFNode ingredientNode = soln.get("ingredient");
	    			            if(ingredientNode != null) {
		    			            
	    			            String ingredienTypeString = ingredientNode.toString();
	    			            String[] ingredienTypeNameArray = ingredienTypeString.split("#");
    					        if(ingredienTypeNameArray.length==2) {
    					        	String ingredienTypeName = ingredienTypeNameArray[1];
    					        	 ingredientTypeList.add(ingredienTypeName);
    	 			                   }
	    			                }		              
	    			            }
	    			        } finally {
	    			        qu.close();
	    			    }
   		
	}
	
private void addToObjectPropertyList() {
				  
		String queryStr =
    			
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				
	    			
    			"SELECT ?objProperty "+ 
    				"	WHERE {"+
    				 "   ?objProperty a owl:ObjectProperty ." +
    		            	"}";	
    // Create the query
    Query q = QueryFactory.create(queryStr);
    // Execute the query on the model
    QueryExecution qu = QueryExecutionFactory.create(q, model);
       
   
    // Execute the query and obtain the results
    try  {
    	ResultSet results = qu.execSelect();
    	while (results.hasNext()) {
    	    QuerySolution soln = results.next();
	        RDFNode objNode = soln.get("objProperty");
	        if(objNode != null) {
	        	String objProString = objNode.toString();
		        String[] objProArray = objProString.split("#");
		        if(objProArray.length==2) {
		        	String objProName = objProArray[1];
		        	 objectPropertyList.add(objProName);
				  }
		        
	        }
	        
	    }
    	   	    
	} finally {
		 qu.close();
	}
   }
	private void addToDataPropertyList() {
			  
	    String queryStr =
	    			
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
	    				    			
				"SELECT ?dataProperty "+ 
				"	WHERE {"+
				 "   ?dataProperty a owl:DatatypeProperty ." +
		            	"}";	
			    
				// Create the query
				Query q = QueryFactory.create(queryStr);
				// Execute the query on the model
				QueryExecution qu = QueryExecutionFactory.create(q, model);
				
				
				// Execute the query and obtain the results
				try  {
				ResultSet results = qu.execSelect();
				while (results.hasNext()) {
				QuerySolution soln = results.next();
				
				RDFNode dataNode = soln.get("dataProperty");
				if(dataNode != null) {
					String dataProString = dataNode.toString();
				    String[] dataProArray = dataProString.split("#");
				    if(dataProArray.length==2) {
				    	String dataProName = dataProArray[1];
				   	    dataPropertyList.add(dataProName);
						
				    }
				    
				}
				
				}
					    
			} finally {
			 qu.close();
			}
		}
	    protected void CovertCSVToRDF() throws JsonProcessingException, IOException, URISyntaxException {
	    	
	    	//External URI is not used for  normal types ...
	    	boolean useExternalURI = false;
    	// If we want to create RDF triples with ontology and data then comment next line model initialization
    	//model = ModelFactory.createDefaultModel();
        
	    	//Prefixes for the serialization
    			model.setNsPrefix("cw", coursework2);
    			model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
    			model.setNsPrefix("gkg", "http://g.co/kg/");
    			model.setNsPrefix("wiki", "http://www.wikidata.org/entity/");
    			model.setNsPrefix("wdt", "http://www.wikidata.org/prop/direct/");
    			model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    			model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
    			model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
    			model.setNsPrefix("rdfs", rdfs);
    			model.setNsPrefix("dbr", "http://dbpedia.org/resource/");
    			model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
    			
    			    			
    		
    	//In a large ontology one would need to find a more automatic way to use the ontology vocabulary. 
        //e.g.,  via matching. In a similar way as we match entities to a large KG like DBPedia or Wikidata
        //Since we are dealing with very manageable ontologies, we can integrate their vocabulary 
        //within the code. E.g.,: coursework2 + City
    	
    	//Mappings may required one or more columns as input and create 1 or more triples for an entity
    	
    	// Maping for Restaurant Type....
    	mappingToRestaurantTypeTriple(column_index.get("name"), coursework2, useExternalURI);
    	
    	// Maping for Address Type....
    	mappingToCreateAddressTypeTriple(column_index.get("address"), coursework2+"Address", useExternalURI);
    	
    	// Maping for City Type....
    	mappingToCreateCityTypeTriple(column_index.get("city"), coursework2+"City", lookupExternalURI);
    	
    	//We give subject column and target type
        
    	mappingToCreateTypeTriple(column_index.get("country"), coursework2+"Country", lookupExternalURI);
    	mappingToCreateTypeTriple(column_index.get("state"), coursework2+"State", lookupExternalURI);
    	mappingToCreateTypeTriple(column_index.get("currency"), coursework2+"Currency", useExternalURI);
    	
    	//We give ItemValue type......
       	mappingToItemValueTypeTriple(column_index.get("item value"), coursework2+"ItemValue", useExternalURI);
       	
       	// Maping for MenuItem Type..........
       	mappingToCreateMenuItemTypeTriple(column_index.get("menu item"), coursework2, useExternalURI);
      
       	// Maping for Ingredient Type....
    	mappingToIngredientTypeTriple(column_index.get("item description"), coursework2 ,useExternalURI);
    
    	//Create Object Property ....................
    	mappingToCreateObjectTriple(column_index.get("country"),column_index.get("state"), coursework2 +"containsState");
    	
    	//Mapping ContainsCity Object Property.....
    	mappingToCreateContainsCityObjectTriple(column_index.get("country"),column_index.get("city"), coursework2 +"containsCity");
    	
    	//Mapping LocatedInAddress Object Property.....
    	mappingToCreateLocatedInAddressObjectTriple(column_index.get("name"),column_index.get("address"), coursework2 +"locatedInAddress");
    	
    	//Mapping hasIngredient Object Property.....
    	mappingToCreateHasIngredientObjectTriple(column_index.get("menu item"), column_index.get("item description"), coursework2+"hasIngredient");
    	
    	//Mapping isIngredient Object Property.....
    	mappingToCreateIsIngredientOfObjectTriple(column_index.get("item description"), column_index.get("menu item"), coursework2+"isIngredientOf");
    	
    	//Mapping locatedInCity Object Property.....
    	mappingToCreateLocatedInCityObjectTriple(column_index.get("name"),column_index.get("city"), coursework2 +"locatedInCity");
    	
    	//Mapping servesMenuItem Object Property.....
    	mappingToCreateServesMenuItemObjectTriple(column_index.get("name"),column_index.get("menu item"), coursework2 +"servesMenuItem");
    	
    	//Mapping servedInRestaurant Object Property.....
    	mappingToCreateServedInRestaurantObjectTriple(column_index.get("menu item"),column_index.get("name"), coursework2 +"servedInRestaurant");
    	
    	//Mapping  itemValue Object Property.....
    	mappingToItemValueObjectTriple(column_index.get("item value"),column_index.get("currency"), coursework2 +"amountCurrency");
    	
    	//Mapping addressLocatedInCity Object Property.....
    	mappingToCreateAddressLocatedInCityObjectTriple(column_index.get("address"),column_index.get("city"), coursework2 +"locatedInCity");
    	
    	//Mapping address's Object Property.....
    	mappingToCreateAddressObjectTriple(column_index.get("address"),column_index.get("state"), coursework2 +"locatedInState");
    	mappingToCreateAddressObjectTriple(column_index.get("address"),column_index.get("country"), coursework2 +"locatedInCountry");
    	
    	//Mapping City's Object Property.....
    	mappingToCreateCityObjectTriple(column_index.get("city"),column_index.get("state"), coursework2 +"locatedInState");
    	mappingToCreateCityObjectTriple(column_index.get("city"),column_index.get("country"), coursework2 +"locatedInCountry");
    	
    	//Mapping hasValue Object Property.....
    	mappingToCreateHasValueObjectTriple(column_index.get("menu item"), column_index.get("item value"), coursework2 + "hasValue");

    	//Create Dataproperty and Literals.......
    	
    	//Mapping restaurantName literals....
    	mappingToCreateRestaurantNameLiteralTriple(column_index.get("name"), column_index.get("name"), coursework2 + "restaurantName", XSDDatatype.XSDstring);
   
    	//Mapping address's literals....
    	mappingToCreateAddressLiteralTriple(column_index.get("address"), column_index.get("address"), coursework2 + "firstLineAddress", XSDDatatype.XSDstring);
    	mappingToCreateAddressLiteralTriple(column_index.get("address"), column_index.get("address"), coursework2 + "postCode", XSDDatatype.XSDstring);

      	//Mapping menuItem literals....
    	mappingToCreateMenuItemLiteralTriple(column_index.get("menu item"), column_index.get("menu item"), coursework2 + "itemName", XSDDatatype.XSDstring);
    	
      	//Mapping itemValue literals....
    	mappingToCreateItemValueLiteralTriple(column_index.get("item value"),column_index.get("item value"), coursework2 +"amount",XSDDatatype.XSDdouble);
    	
      	//Mapping City RDFS label literals....
    	mappingToCreateCityLiteralTriple(column_index.get("city"),column_index.get("city"),rdfs+"label", XSDDatatype.XSDstring);
    	
    	//MappingRDFS label literals....    	
    	mappingToCreateLiteralTriple(column_index.get("state"),column_index.get("state"),rdfs+"label", XSDDatatype.XSDstring);
    	mappingToCreateLiteralTriple(column_index.get("country"),column_index.get("country"),rdfs+"label", XSDDatatype.XSDstring);
    	mappingToCreateLiteralTriple(column_index.get("currency"),column_index.get("currency"),rdfs+"label",XSDDatatype.XSDstring);
    	
     	
    }
	    
	// Maping for Restaurant Type....
	protected void mappingToRestaurantTypeTriple(int subject_column, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
        
   	 boolean isFirstRow = true;
    	
        for (String[] row : csv_file) {
    		
    		//Ignore rows with less elements than expected
    		if (row.length<column_index.size())
    			continue;
    		//Ignore the first row as it contains column name.....
    		if (isFirstRow) {
    	        isFirstRow = false; // Update the flag for subsequent iterations
    	        continue;
    	    }
    		//Ignore if subject column is empty.....
    		if((row[subject_column] == "")) {
    			continue;
    		}
    		else {
       			//Create Subject String with Restaurant name with City and Postcode.....
       			String subject = row[subject_column].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
           		subject = subject.replaceAll("[^a-zA-Z0-9]+", "_");
       			String subject_uri;
           		
             	//We reuse URI for a Restaurant if it is already created.....
           		if (stringToURI.containsKey(subject))
           			subject_uri=stringToURI.get(subject);
                   else
                	 //We create the fresh URI for a Restaurant in the dataset
                  	  subject_uri=createURIForEntity(subject, useExternalURI);
           		
           		Resource subject_resource = model.createResource(subject_uri);
           		
           		//Restaurant category from column 6 called categories...
           		String resCategoryName = row[6];
           		//Split possible categories based on commas, spaces, & and 'and' word......
       			String[] allResCategory = resCategoryName.split(",\\s*|\\s+and\\s+|\\s*&\\s*");
       		
       			boolean foundResType = false;
       			
       			for(String resCategory : allResCategory) {
       				//System.out.println(resCategory);
       	       		
       			//Check Restaurant category is in the ontology category vocabulary or not...	
       				if(restaurantTypeList.contains(resCategory.toLowerCase().trim())) {
       					foundResType = true;
       					String resCatName = convertTocamelCase(resCategory);
       					Resource type_resource = model.createResource(class_type_uri+resCatName);
       	        		model.add(subject_resource, RDF.type, type_resource);
       					}
       				}
       			//If No Restaurant category is found then default is Restaurant type...	
       			if(foundResType == false) {
       				
       				Resource type_resource = model.createResource(class_type_uri+"Restaurant");
   	        		model.add(subject_resource, RDF.type, type_resource);
   	        		
       			}
       		}
       	}
    }
    protected void mappingToCreateAddressTypeTriple(int subject_column_index, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
        
      	 boolean isFirstRow = true;
       	
           for (String[] row : csv_file) {
           	//Ignore rows with less elements than expected
       		if (row.length<column_index.size())
       			continue;
       		//Ignore the first row as it contains column name.....
       		if (isFirstRow) {
       	        isFirstRow = false; // Update the flag for subsequent iterations
       	        continue;
       	    }
       		//Ignore if subject column is empty.....
       		if((row[subject_column_index] == "")) {
       			continue;
       		}
       		else {
       		//Create Subject String with First Line address with City and Country.....
       			String subject = row[subject_column_index].toLowerCase()+" "+row[2]+" "+row[3];
           		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
       			String subject_uri;
           		
           		//We reuse URI if it is already created.....
           		if (stringToURI.containsKey(subject))
           			subject_uri=stringToURI.get(subject);
                   else
                	 //We create the fresh URI if it is not created......
                  	  subject_uri=createURIForEntity(subject, useExternalURI);
           		
          		
          		//TYPE TRIPLE    		
          		Resource subject_resource = model.createResource(subject_uri);
          		Resource type_resource = model.createResource(class_type_uri);
          		
          		model.add(subject_resource, RDF.type, type_resource);
          	
         		}
       	}
     }
    protected void mappingToCreateCityTypeTriple(int subject_column, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
      	 boolean isFirstRow = true;
       	
           for (String[] row : csv_file) {
           	//Ignore rows with less elements than expected
       		if (row.length<column_index.size())
       			continue;
       		//Ignore the first row as it contains column name.....
       		if (isFirstRow) {
       	        isFirstRow = false; // Update the flag for subsequent iterations
       	        continue;
       	    }
       		//Ignore if subject column is empty.....
       		if((row[subject_column] == "")) {
       			continue;
       		}
       		else {
       			String subject;
       			String subject_uri;
 	     		
       			if(!lookupExternalURI)
       			//Creating URI for City with City and State ......
     	 			subject = row[subject_column].toLowerCase()+" "+row[5].toLowerCase();
       			else subject = row[subject_column];
     	     		
       			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
       			
     	     		//We reuse URI if it is already created.....
               		if (stringToURI.containsKey(subject))
               			subject_uri=stringToURI.get(subject);
                       else
                    	 //We create the fresh URI if it is not created......
                      	  subject_uri=createURIForEntity(subject, useExternalURI);
               		
     	    		
     	    		//TYPE TRIPLE    		
     	    		Resource subject_resource = model.createResource(subject_uri);
     	    		Resource type_resource = model.createResource(class_type_uri);
     	    		
     	    		model.add(subject_resource, RDF.type, type_resource);
     	    	
     	   		}
     	 	
     	 	}
     	 	
     	 }
    protected void mappingToCreateMenuItemTypeTriple(int subject_column, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
        
   	 boolean isFirstRow = true;
    	
        for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected or subject column Emptty
    		if ((row.length<column_index.size())||((row[subject_column] == "")))
    			continue;
    		//Ignore the first row as it contains column name.....
    		if (isFirstRow) {
    	        isFirstRow = false; // Update the flag for subsequent iterations
    	        continue;
    	    }
    		else {
    			// Used item name with restaurant name and located city to create unique URIs.....
    			String subject = row[subject_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
    			String subject_uri;
        		
    			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
    			
    			//We reuse URI if it is already created.....
           		if (stringToURI.containsKey(subject))
           			subject_uri=stringToURI.get(subject);
                else
                //We create the fresh URI if it is not created......
                   subject_uri=createURIForEntity(subject, useExternalURI);
           		
        		Resource subject_resource = model.createResource(subject_uri);
        		
        		// Combined item description and item name to identify what type of menu item is it....
        		String itemDescription = row[7].toLowerCase()+" "+row[10].toLowerCase();
        		
        	
        		for(String typedPizza : menuItemTypeList ) {
        			if(itemDescription.contains(typedPizza.toLowerCase())) {
        				//TYPE TRIPLE    		
               		Resource type_resource = model.createResource(class_type_uri+convertTocamelCase(typedPizza));
               		model.add(subject_resource, RDF.type, type_resource);
        			}
        			  			
        		}
        	
    	}
      }
    }
	protected void mappingToIngredientTypeTriple(int subject_column, String  class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
	   	 boolean isFirstRow = true;
	 	
	     for (String[] row : csv_file) {
	 		
	    	//Ignore rows with less elements than expected
	 		if (row.length<column_index.size())
	 			continue;
	 		//Ignore the first row as it contains column name.....
	 		if (isFirstRow) {
	 	        isFirstRow = false; // Update the flag for subsequent iterations
	 	        continue;
	 	    }
	 		//Ignore if subject column is empty.....
	 		if((row[subject_column] == "")) {
	 			continue;
	 		}
	 		else {
	      	 			
	      	 			
	       	 		    String itemIngredients = row[subject_column].toLowerCase();
	       	 			String[] itemIngList = itemIngredients.split("(,| or | with | our | and | topped | on | in | a )");
	       	 		    for(String ingName : itemIngList) {
	       	 		    	for(String typeIng : ingredientTypeList)
	       	 				if(ingName.contains(typeIng.toLowerCase())) {
	      	 					
	      	 					String subject = ingName.trim();
	      	 					subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
	      	 					subject = subject.replaceAll("_+$", ""); // Remove any trailing underscores
	      	 					String subject_uri;
	      	 					
	      	 					//We reuse URI if it is already created.....
	      	 	           		if (stringToURI.containsKey(subject))
	      	 	           			subject_uri=stringToURI.get(subject);
	      	 	                 else
	      	 	                	 //We create the fresh URI if it is not created......
	      	 	                  	  subject_uri=createURIForEntity(subject, useExternalURI);
	      	 	           		
	   		   	 		Resource subject_resource = model.createResource(subject_uri);
	      	    		Resource type_resource = model.createResource(class_type_uri+typeIng);
	      	    		
	      	    		model.add(subject_resource, RDF.type, type_resource);
	      	    		
	      	    		//creating label for ingredents..........
	      	    		
	      	    		 Property predicate_resource = RDFS.label;
	      		         Literal lit = model.createTypedLiteral(subject, XSDDatatype.XSDstring);
	     		 		
	     		 		model.add(subject_resource, predicate_resource, subject);

	      	      	
	      	 				}
	      	 			}
	      	 			
	      	   		}
	      	 	
	      	 	}
	   	 	
	   	 }
	 protected void mappingToItemValueTypeTriple(int subject_column_index, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column_index] == "")) {
     			continue;
     		}
     		else {
     			//ItemValue URIs were created with item value and currency .....
   	 			String subject = row[subject_column_index].toLowerCase()+" "+row[9];
   	 			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
   	     		
   	 			String subject_uri;
   	     		
   	     		//We reuse URI if it is already created.....
           		if (stringToURI.containsKey(subject))
           			subject_uri=stringToURI.get(subject);
                   else
                	 //We create the fresh URI if it is not created......
                  	  subject_uri=createURIForEntity(subject, useExternalURI);
           		
   	    		
   	    		//TYPE TRIPLE    		
   	    		Resource subject_resource = model.createResource(subject_uri);
   	    		Resource type_resource = model.createResource(class_type_uri);
   	    		
   	    		//Adding new Triples....
   	    		model.add(subject_resource, RDF.type, type_resource);
   	    	
   	   		}
   	  	}
   	  }
   	
	protected void mappingToCreateTypeTriple(int subject_column_index, String class_type_uri, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
	      
		boolean isFirstRow = true;
	    	
	        for (String[] row : csv_file) {
	        	//Ignore rows with less elements than expected
	    		if (row.length<column_index.size())
	    			continue;
	    		//Ignore the first row as it contains column name.....
	    		if (isFirstRow) {
	    	        isFirstRow = false; // Update the flag for subsequent iterations
	    	        continue;
	    	    }
	    		//Ignore if subject column is empty.....
	    		if((row[subject_column_index] == "")) {
	    			continue;
	    		}
	    		String subject = row[subject_column_index];
	    		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	    		String subject_uri;
	    		
	    			//We reuse URI if it is already created.....
	       		if (stringToURI.containsKey(subject))
	       			subject_uri=stringToURI.get(subject);
	            else
	            	 //We create the fresh URI if it is not created......
	              	  subject_uri=createURIForEntity(subject, useExternalURI);
	       		
	    		
	    		//TYPE TRIPLE    		
	    		Resource subject_resource = model.createResource(subject_uri);
	    		Resource type_resource = model.createResource(class_type_uri);
	    		
	    		model.add(subject_resource, RDF.type, type_resource);
	    	
	    	}
	    	
	    }
   
    private void mappingToCreateHasValueObjectTriple(int subject_column, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     			else {
     			// Used item name with restaurant name and located city to create menuItem as type was created combining these ...
     	 			String subject =row[subject_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
     	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
     	 			// hasValue is combined with item value and currency....
     	 			String object =  row[object_column].toLowerCase()+" "+row[9];
     	 			object =  object.replaceAll("[^a-zA-Z0-9]", "_");
     	 					if (is_nan(object))
            			continue;
     	 					
            		// Retrieve the URIs for subject and object....... 
     	 		    String subject_uri = stringToURI.get(subject);
     	 		    String object_uri = stringToURI.get(object);
  		                
  		            
  		            //New triple            
  		            Resource subject_resource = model.createResource(subject_uri);
  		            Property predicate_resource = model.createProperty(predicate);
  		            Resource object_resource = model.createResource(object_uri);
  		         
  		            //Add triple to the model... 
  		            model.add(subject_resource, predicate_resource, object_resource);
  		    		
  		            
     	 				}
     	 			}
     	 		}
  
    private void mappingToCreateLocatedInCityObjectTriple(int subject_column, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     		else {
      	 			// created URIs combining with city and postcode ...
      	 			String subject = row[subject_column].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
      	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
      	 			String object;
      	 			if(!lookupExternalURI)
      	       			//Creating URI for City with City and State ......
      	     	 			object = row[object_column].toLowerCase()+" "+row[5].toLowerCase();
      	       		else object = row[object_column];
      	 			
      	 			object =  object.replaceAll("[^a-zA-Z0-9]", "_");	
      	 			
      	 					if (is_nan(object))
             			continue;
             		
      	 			// Retrieve URIs from maps....
      	 		    String subject_uri = stringToURI.get(subject);
      	 		    String object_uri = stringToURI.get(object);
   		                
   		            
   		            //New triple            
   		            Resource subject_resource = model.createResource(subject_uri);
   		            Property predicate_resource = model.createProperty(predicate);
   		            Resource object_resource = model.createResource(object_uri);
   		            
   		            //Add RDF triple to the model....
   		            model.add(subject_resource, predicate_resource, object_resource);
   		    		
   		            
      	 				}
      	 			}
      	 		}
    private void mappingToCreateServedInRestaurantObjectTriple(int subject_column, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     			else {
    	 			//MenuItem URIs for menu item created with item name, restaurant name and city.....
    	 			String subject = row[subject_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
    	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
    	 			//Restaurant URIs restaurant name, city and postcode....
    	 			String object = row[object_column].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
    	 			object =  object.replaceAll("[^a-zA-Z0-9]+", "_");
    	 			
    	 					if (is_nan(object))
           			continue;
           		
    	 		   //Retrieve already created URIs....    	 				
    	 			String subject_uri = stringToURI.get(subject);
    	 		    String object_uri = stringToURI.get(object);
    	 		    
    	 		    //New triple            
 		            Resource subject_resource = model.createResource(subject_uri);
 		            Property predicate_resource = model.createProperty(predicate);
 		            Resource object_resource = model.createResource(object_uri);
 		            
 		            //Add new RDF triples .....
 		            model.add(subject_resource, predicate_resource, object_resource);
 		    		
 		            
    	 				}
    	 			}
    	 		}
     	 
	
    private void mappingToCreateServesMenuItemObjectTriple(int sub_col, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
     	 	
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[sub_col] == "")) {
     			continue;
     		}
     		else {
     			    //Restaurant URIs restaurant name, city and postcode....
     	 			String subject = row[sub_col].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
     	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
     	 			
     	 			//MenuItem URIs for menu item created with item name, restaurant name and city.....
     	 			String object = row[object_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
     	 			object =  object.replaceAll("[^a-zA-Z0-9]+", "_");
     	 			
     	 					if (is_nan(object))
            			continue;
            		
     	 			//Retrieving URIs from the map...
     	 		    String subject_uri = stringToURI.get(subject);
     	 		    String object_uri = stringToURI.get(object);
     	 		    
     	 		    //New triple            
  		            Resource subject_resource = model.createResource(subject_uri);
  		            Property predicate_resource = model.createProperty(predicate);
  		            Resource object_resource = model.createResource(object_uri);
  		            
  		            //Adding new RDF triples ....
  		            model.add(subject_resource, predicate_resource, object_resource);
  		    		
  		            
     	 				}
     	 			}
     	 		}
      	 
	
    protected void mappingToCreateObjectTriple(int subject_column, int object_column, String predicate) {
        
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}    		
    		
    		String subject = row[subject_column];
    		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
    		String object = row[object_column];
    		object =  object.replaceAll("[^a-zA-Z0-9]", "_");
    		
    		if (is_nan(object))
    			continue;
    		

            //Uri as already created
            String subject_uri = stringToURI.get(subject);
            String object_uri = stringToURI.get(object);
                
            
            //New triple            
            Resource subject_resource = model.createResource(subject_uri);
            Property predicate_resource = model.createProperty(predicate);
            Resource object_resource = model.createResource(object_uri);
            
    		//Adding new REF triples .....
    		model.add(subject_resource, predicate_resource, object_resource);
    		
    	}
    }
    protected void mappingToCreateContainsCityObjectTriple(int subject_column, int object_column, String predicate) {
        
   	 boolean isFirstRow = true;
    	
        for (String[] row : csv_file) {
    		
        	//Ignore rows with less elements than expected
    		if (row.length<column_index.size())
    			continue;
    		//Ignore the first row as it contains column name.....
    		if (isFirstRow) {
    	        isFirstRow = false; // Update the flag for subsequent iterations
    	        continue;
    	    }
    		//Ignore if subject column is empty.....
    		if((row[subject_column] == "")) {
    			continue;
    		}
    		
   		String subject = row[subject_column];
   		String object;
   		if(!lookupExternalURI)
     			//Creating URI for City with City and State ......
   	 			object = row[object_column].toLowerCase()+" "+row[5].toLowerCase();
     		else object = row[object_column];
   		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
   		object =  object.replaceAll("[^a-zA-Z0-9]", "_");
   		if (is_nan(object))
   			continue;
   		
   		//Uri as already created
           String subject_uri = stringToURI.get(subject);
           String object_uri = stringToURI.get(object);
              
           
           //New triple            
           Resource subject_resource = model.createResource(subject_uri);
           Property predicate_resource = model.createProperty(predicate);
           Resource object_resource = model.createResource(object_uri);
           
   		//Adding new RDFTriples......
   		model.add(subject_resource, predicate_resource, object_resource);
   		
   	}
   }

    
      
    protected void mappingToItemValueObjectTriple(int subject_column, int object_column, String predicate) {
    
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
     		
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     		
     		// ItemVlaue URIs with item value and currency...
     		String subject = row[subject_column].toLowerCase()+" "+row[9];
    	    String object = row[object_column];
    		
    	    subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
    	    object =  object.replaceAll("[^a-zA-Z0-9]", "_");
    		if (is_nan(object))
    			continue;
    	
    	 		
	     		//We already created URIs.....
	     	    String subject_uri = stringToURI.get(subject);
	            String object_uri = stringToURI.get(object);
	                
	            //New triple            
	            Resource subject_resource = model.createResource(subject_uri);
	            Property predicate_resource = model.createProperty(predicate);
	            Resource object_resource = model.createResource(object_uri);
	            
	            //Adding new RDF triples......
	    		model.add(subject_resource, predicate_resource, object_resource);
	    	
	   		}
	 	
	 	}
	 
    protected void mappingToCreateLocatedInAddressObjectTriple(int sub_col, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[sub_col] == "")) {
     			continue;
     		}
     		else {
     				//Restaurant name URIs combined with restaurant name, city and postcode.....
      	 			String subject = row[sub_col].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
      	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
      	 			//Address URIs combined with firest line of address, city and country....
      	 			String object = row[object_column].toLowerCase()+" "+row[2]+" "+row[3];
      	 			object =  object.replaceAll("[^a-zA-Z0-9]", "_");
      	 			if (is_nan(object))
             			continue;
             		
      	 			//Retrieving already created URIs..... 
      	 		    String subject_uri = stringToURI.get(subject);
      	 		    String object_uri = stringToURI.get(object);
   		                
   		            
   		            //New triple            
   		            Resource subject_resource = model.createResource(subject_uri);
   		            Property predicate_resource = model.createProperty(predicate);
   		            Resource object_resource = model.createResource(object_uri);
   		            
   		            //Adding RDF triples .....
   		            model.add(subject_resource, predicate_resource, object_resource);
   		    		
     				}
      	 		}
      	 	}
      	 	
      	 
    protected void mappingToCreateHasIngredientObjectTriple(int subject_column, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
     		
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     		else {
   	 			
   	 			String itemIngredients = row[object_column].toLowerCase();
   	 			
   	 			//List ingredients from description for comma, or, with,our,and,topped,on,in .....
   	 			String[] itemIngList = itemIngredients.split("(,| or | with | our | and | topped | on | in | a )");
   	 			
   	 			//MenuItem is with item name and reasaurant name and city.....
   	 			String subject = row[subject_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
   	 			subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
   	 			
   	 			//Retrieving URIs... 
   	 		    String subject_uri = stringToURI.get(subject);
   	 	
   	 		    //Check ingredient from the description contains ingredientType vocabulary from ontology .....
   	 		    for(String ingName : itemIngList) {
   	 		    	for(String typeIng : ingredientTypeList)
	   	 				if(ingName.contains(typeIng.toLowerCase())) {
	   	 				
	   	 				String object =  ingName.trim();
	   	 			    object = object.replaceAll("[^a-zA-Z0-9]+", "_");
	   	 			    object = object.replaceAll("_+$", ""); // Remove any trailing underscores
			   	 		//retrive URI for ingredientType ..	
			   	 		String object_uri = stringToURI.get(object);
			                
			            
			            //New triple            
			            Resource subject_resource = model.createResource(subject_uri);
			            Property predicate_resource = model.createProperty(predicate);
			            Resource object_resource = model.createResource(object_uri);
			            
			            //Add new RDF triple......
			            model.add(subject_resource, predicate_resource, object_resource);
			    	}
   	 			}
   	 		}
   	 	}
   	 }
    protected void mappingToCreateIsIngredientOfObjectTriple(int subject_column, int object_column,String predicate) throws JsonProcessingException, IOException, URISyntaxException {
    	 boolean isFirstRow = true;
     	
         for (String[] row : csv_file) {
     		
        	//Ignore rows with less elements than expected
     		if (row.length<column_index.size())
     			continue;
     		//Ignore the first row as it contains column name.....
     		if (isFirstRow) {
     	        isFirstRow = false; // Update the flag for subsequent iterations
     	        continue;
     	    }
     		//Ignore if subject column is empty.....
     		if((row[subject_column] == "")) {
     			continue;
     		}
     			else {
     				String itemIngredients = row[subject_column].toLowerCase();
      	 			
      	 			//MenuItem uri is created with item name, restaurant name and city ... 
      	 			String object = row[object_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
      	 			object = object.replaceAll("[^a-zA-Z0-9]+", "_");
      	 		   
      	 			String object_uri = stringToURI.get(object);
      	 		
      	 		    //List ingredients from description for comma, or, with,our,and,topped,on,in .....
       	 			String[] itemIngList = itemIngredients.split("(,| or | with | our | and | topped | on | in | a )");
    	 			
       	 		 //Check ingredient from the description contains ingredientType vocabulary from ontology .....
        	 		for(String ingName : itemIngList) {
    	 		      for(String typeIng : ingredientTypeList)
    	 				if(ingName.contains(typeIng.toLowerCase())) {
    	 				
	    	 				String subject =  ingName.trim();
	    	 				subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
	    	 				subject = subject.replaceAll("_+$", ""); // Remove any trailing underscores
		 		   	 		String subject_uri = stringToURI.get(subject);
		 		            
		 		   	 		//New triple            
		   		            Resource subject_resource = model.createResource(subject_uri);
		   		            Property predicate_resource = model.createProperty(predicate);
		   		            Resource object_resource = model.createResource(object_uri);
		   		            
		   		            //ADding new RDF triples ....
		   		            model.add(subject_resource, predicate_resource, object_resource);
		   				}
      	 			}
      	 		}
      	    }
      	}
      	
  protected void mappingToCreateAddressLocatedInCityObjectTriple(int subject_column, int object_column, String predicate) throws JsonProcessingException, IOException, URISyntaxException {
     
	 boolean isFirstRow = true;
 	
     for (String[] row : csv_file) {
    	//Ignore rows with less elements than expected
 		if (row.length<column_index.size())
 			continue;
 		//Ignore the first row as it contains column name.....
 		if (isFirstRow) {
 	        isFirstRow = false; // Update the flag for subsequent iterations
 	        continue;
 	    }
 		//Ignore if subject column is empty.....
 		if((row[subject_column] == "")) {
 			continue;
 		}
 		//Address URIs combined with first line of address, city and country....
 		String subject = row[subject_column].toLowerCase()+" "+row[2]+" "+row[3];
 		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
 		String object;
 		
 		if(!lookupExternalURI)
     			//Creating URI for City with City and State ......
   	 			object = row[object_column].toLowerCase()+" "+row[5].toLowerCase();
     		else object = row[object_column];
 		object =  object.replaceAll("[^a-zA-Z0-9]", "_");
 				if (is_nan(object))
	    			continue;
	    		

	            //Uri as already created
	            String subject_uri = stringToURI.get(subject);
	            String object_uri = stringToURI.get(object);
	                
	            
	            //New triple            
	            Resource subject_resource = model.createResource(subject_uri);
	            Property predicate_resource = model.createProperty(predicate);
	            Resource object_resource = model.createResource(object_uri);
	            
	    		//Adding new RDF triples .....
	    		model.add(subject_resource, predicate_resource, object_resource);

	   		}
	 	
	 	}
protected void mappingToCreateAddressObjectTriple(int subject_column, int object_column, String predicate) throws JsonProcessingException, IOException, URISyntaxException {
     
	 boolean isFirstRow = true;
 	
     for (String[] row : csv_file) {
 		
    	//Ignore rows with less elements than expected
 		if (row.length<column_index.size())
 			continue;
 		//Ignore the first row as it contains column name.....
 		if (isFirstRow) {
 	        isFirstRow = false; // Update the flag for subsequent iterations
 	        continue;
 	    }
 		//Ignore if subject column is empty.....
 		if((row[subject_column] == "")) {
 			continue;
 		}
 		
 		//Address URIs combined with first line of address, city and country....
 			String subject = row[subject_column].toLowerCase()+" "+row[2]+" "+row[3];
 			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	 		String object = row[object_column];
	 		object =  object.replaceAll("[^a-zA-Z0-9]", "_");		
	    		if (is_nan(object))
	    			continue;
	    		

	            //Uri as already created
	            String subject_uri = stringToURI.get(subject);
	            String object_uri = stringToURI.get(object);
	                
	            
	            //New triple            
	            Resource subject_resource = model.createResource(subject_uri);
	            Property predicate_resource = model.createProperty(predicate);
	            Resource object_resource = model.createResource(object_uri);
	            
	    	   //Adding new RDF triples.....	
	    		model.add(subject_resource, predicate_resource, object_resource);
	   		}
	 	}

protected void mappingToCreateCityObjectTriple(int subject_column, int object_col, String predicate) throws JsonProcessingException, IOException, URISyntaxException {
     
	 boolean isFirstRow = true;
 	
     for (String[] row : csv_file) {
    	//Ignore rows with less elements than expected
 		if (row.length<column_index.size())
 			continue;
 		//Ignore the first row as it contains column name.....
 		if (isFirstRow) {
 	        isFirstRow = false; // Update the flag for subsequent iterations
 	        continue;
 	    }
 		//Ignore if subject column is empty.....
 		if((row[subject_column] == "")) {
 			continue;
 		}
 		else {
	 		//City URIs combined with city and state....
	 		String subject ;
	 		
	 		if(!lookupExternalURI)
     			//Creating URI for City with City and State ......
   	 			subject = row[subject_column].toLowerCase()+" "+row[5].toLowerCase();
     		else subject = row[subject_column];
   	
		    String object = row[object_col];
		    
		    subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
		    object =  object.replaceAll("[^a-zA-Z0-9]", "_");
	 		
	    		if (is_nan(object))
	    			continue;
	    		

	            //Uri as already created
	            String subject_uri = stringToURI.get(subject);
	            String object_uri = stringToURI.get(object);
	                
	            
	            //New triple            
	            Resource subject_resource = model.createResource(subject_uri);
	            Property predicate_resource = model.createProperty(predicate);
	            Resource object_resource = model.createResource(object_uri);
	            
	    		//Adding new RED triples .....
	    		model.add(subject_resource, predicate_resource, object_resource);

	   			}
     		}
	 	}
protected void mappingToCreateAddressLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
		
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			//Address URI with first line of address, city and Country
	 		String subject = row[subject_column].toLowerCase()+" "+row[2]+" "+row[3];
	 		subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	 		
	 		String lit_value = row[object_column];
	 		
	 		if (is_nan(lit_value))
	 			continue;
	 		

	         //Uri as already created
	         String entity_uri = stringToURI.get(subject);
	             
	         
	         //New triple            
	         Resource subject_resource = model.createResource(entity_uri);
	         Property predicate_resource = model.createProperty(predicate);
	         
	 		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

	 		
	 		model.add(subject_resource, predicate_resource, lit);
	 		
	 	}
	}
	         
 }

protected void mappingToCreateRestaurantNameLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			//RestaurantName URI with restaurant name, city and postcode.....
		String subject = row[subject_column].toLowerCase()+" "+row[2].toLowerCase()+" "+row[4].toLowerCase();
		subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
		String lit_value = row[object_column];
		
		if (is_nan(lit_value))
			continue;
		

        //Uri as already created
        String entity_uri = stringToURI.get(subject);
            
        
        //New triple            
        Resource subject_resource = model.createResource(entity_uri);
        Property predicate_resource = model.createProperty(predicate);
        
		//Literal
        Literal lit = model.createTypedLiteral(lit_value, datatype);

		//Adding newRDF triple....
		model.add(subject_resource, predicate_resource, lit);
		
		}
	}
        
}
protected void mappingToCreateMenuItemLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			//MenuItem URIs with item name, Restaurant and city
	 		String subject = row[subject_column].toLowerCase()+" at "+row[0].toLowerCase()+" in "+row[2].toLowerCase();
	 		subject =  subject.replaceAll("[^a-zA-Z0-9]+", "_");
	 		
	 		String lit_value = row[object_column];
	 		
	 		if (is_nan(lit_value))
	 			continue;
	 		

	         //Uri as already created
	         String entity_uri = stringToURI.get(subject);
	             
	         
	         //New triple            
	         Resource subject_resource = model.createResource(entity_uri);
	         Property predicate_resource = model.createProperty(predicate);
	         
	 		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

	 		//Adding new RDF Triple
	 		model.add(subject_resource, predicate_resource, lit);
	 		
	 	}
	}	
}
protected void mappingToCreateItemValueLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			//ItemValue URIs with item value and currency..
			String subject = row[subject_column].toLowerCase()+" "+row[9];
			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
			
			String lit_value = row[object_column];
			if(lit_value.equals("item value")||(lit_value == "")) 
	 			continue;
	 		
	 		
	 		if (is_nan(lit_value))
	 			continue;
	 		

	         //Uri as already created
	         String entity_uri = stringToURI.get(subject);
	             
	         
	         //New triple            
	         Resource subject_resource = model.createResource(entity_uri);
	         Property predicate_resource = model.createProperty(predicate);
	         
	 		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

	 		//Adding new RDF triples....
	 		model.add(subject_resource, predicate_resource, lit);
	 		
	 	}
    }
}
protected void mappingToCreateRDFSLabelTriple(int subject_column,int object_column, XSDDatatype datatype) {
	 
	boolean isFirstRow = true;
	 	
    for (String[] row : csv_file) {
		
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			String subject = row[subject_column];
			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	 		String lit_value = row[object_column];
	 		
	 		if (is_nan(lit_value))
	 			continue;

	 		//Uri as already created
	         String entity_uri = stringToURI.get(subject);
	        
	         //New triple            
	         Resource subject_resource = model.createResource(entity_uri);

	         Property predicate_resource = RDFS.label;
     
		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

		//Adding new RDF triples....
	         model.add(subject_resource, predicate_resource, lit);
		}
    }
}
protected void mappingToCreateLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}else {
			String subject = row[subject_column];
			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	 		String lit_value = row[object_column];
	 		
	 		if (is_nan(lit_value))
	 			continue;
	 		

	         //Uri as already created
	         String entity_uri = stringToURI.get(subject);
	             
	         
	         //New triple            
	         Resource subject_resource = model.createResource(entity_uri);
	         Property predicate_resource = model.createProperty(predicate);
	         
	 		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

	 		//Adding new RDF triples.....
	 		model.add(subject_resource, predicate_resource, lit);
	 		
	 	}
	 	
    }
 }

protected void mappingToCreateCityLiteralTriple(int subject_column, int object_column, String predicate, XSDDatatype datatype) {
    
	 boolean isFirstRow = true;
	
    for (String[] row : csv_file) {
   	//Ignore rows with less elements than expected
		if (row.length<column_index.size())
			continue;
		//Ignore the first row as it contains column name.....
		if (isFirstRow) {
	        isFirstRow = false; // Update the flag for subsequent iterations
	        continue;
	    }
		//Ignore if subject column is empty.....
		if((row[subject_column] == "")) {
			continue;
		}
		else {
			//City URIs combined with city and state.....
			String subject ;
			if(!lookupExternalURI)
     			//Creating URI for City with City and State ......
   	 			subject = row[subject_column].toLowerCase()+" "+row[5].toLowerCase();
     		else subject = row[subject_column];
			
			subject =  subject.replaceAll("[^a-zA-Z0-9]", "_");
	 		String lit_value = row[object_column];
	 		
	 		if (is_nan(lit_value))
	 			continue;
	 		
	 		
	         //Uri as already created
	         String entity_uri = stringToURI.get(subject);
	           //New triple            
	         Resource subject_resource = model.createResource(entity_uri);
	         Property predicate_resource = model.createProperty(predicate);
	         
	 		//Literal
	         Literal lit = model.createTypedLiteral(lit_value, datatype);

	 		//Adding new Triples ....
	 		model.add(subject_resource, predicate_resource, lit);
	 		
	 	}
    }
	         
}


 public void saveGraph(Model model, String file_output) throws FileNotFoundException {
     
	    //SAVE/SERIALIZE GRAPH
		
	    OutputStream out = new FileOutputStream(file_output);
	    RDFDataMgr.write(out, model, RDFFormat.TURTLE);	       

	}	
  private String convertTocamelCase(String inputString) {
	// TODO Auto-generated method stub
	 StringBuilder camelCaseString = new StringBuilder();
     boolean capitalizeNext = true;

     for (char c : inputString.toCharArray()) {
         if (Character.isLetterOrDigit(c)) {
             if (capitalizeNext) {
                 camelCaseString.append(Character.toUpperCase(c));
                 capitalizeNext = false;
             } else {
                 camelCaseString.append(Character.toLowerCase(c));
             }
         } else {
             capitalizeNext = true;
         }
        
     }
     return camelCaseString.toString();
}
protected String createURIForEntity(String name, boolean useExternalURI) throws JsonProcessingException, IOException, URISyntaxException {
     
     //We create fresh URI (default option)
     stringToURI.put(name, coursework2 + processLexicalName(name));
     
     if (useExternalURI) {//We connect to online KG
        //Get URI from depedia ..
    	 // String uri = getExternalKGURI(name);
    
    	 //Get URI from GoogleKG...
    	// String uri = getExternalKGURIGoogle(name);
         
         //Get URI from WikiKG.....
         String uri = getExternalKGURIWikidata(name);
         
        // System.out.println(name+" wiki KG " + uri);
         if (!uri.equals(""))
         	stringToURI.put(name, uri);
     }
     return stringToURI.get(name);
 
 }
 protected String processLexicalName(String name) {
 	
 	//Remove potential spaces and other characters not allowed in URIs
     
     //This method may need to be extended
 	//Other problematic characters: 
     //{", "}", "|", "\", "^", "~", "[", "]", and "`"
 	
 	return name.replaceAll(" ", "_").replaceAll("\\(", "").replaceAll("\\)", "");
 }
 
 protected String getExternalKGURIGoogle(String name) throws JsonProcessingException, IOException, URISyntaxException {

	    // Approximate solution: We get the entity with highest lexical similarity
	    // The use of context may be necessary in some cases

	    Set<String> types = new HashSet<>();
	    types.add("country");
	    types.add("state");
	    types.add("city");

	    Set<String> languages = new HashSet<>();
	    languages.add("en");

	    TreeSet<KGEntity> entities = googleKGLookup.getEntities(name, "5", types, languages, 0.8);

	    double current_sim = -1.0;
	    String current_uri = "";

	    for (KGEntity ent : entities) {
	        if (ent != null) {
	            double isub_score = isub.score(name, ent.getName());
	            if (current_sim < isub_score) {
	                current_uri = ent.getId();
	                current_sim = isub_score;
	            }
	        }
	    }

	    return current_uri;
	}

 protected String getExternalKGURIWikidata(String name) throws JsonProcessingException, IOException, URISyntaxException {

	    // Approximate solution: We get the entity with highest lexical similarity
	    // The use of context may be necessary in some cases

	    Set<KGEntity> entities = wikiKGLookup.getKGEntities(name, 5, "en");
	    // print("Entities from Wikidata Lookup:")
	    double current_sim = -1.0;
	    String current_uri = "";

	    for (KGEntity ent : entities) {
	        if (ent != null) {
	            double isub_score = isub.score(name, ent.getName());
	            if (current_sim < isub_score) {
	                current_uri = ent.getId();
	                current_sim = isub_score;
	            }
	        }
	    }

	    return current_uri;
	}

 
 public void performReasoning(String ontology_file) {
     
     	
	System.out.println("Data triples from CSV: '" + model.listStatements().toSet().size() + "'.");
		        
     //We should load the ontology first        
     Dataset dataset = RDFDataMgr.loadDataset(ontology_file);
     model.add(dataset.getDefaultModel().listStatements().toList());
     
     
     System.out.println("Triples including ontology: '" + model.listStatements().toSet().size() + "'.");
     
     Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();		
		inf_model = ModelFactory.createInfModel(reasoner, model);
		
     
     System.out.println("Triples after reasoning: '" + inf_model.listStatements().toSet().size() + "'.");
     
     
 }
  public static String fromCamelCaseToCamel(String inputString) {
     StringBuilder camelString = new StringBuilder();

     for (int i = 0; i < inputString.length(); i++) {
         char currentChar = inputString.charAt(i);

         // If the current character is uppercase, add a space followed by its lowercase equivalent
         if (Character.isUpperCase(currentChar)) {
             if (i != 0) {
                 camelString.append(" ");
             }
             camelString.append(Character.toLowerCase(currentChar));
         } else {
             camelString.append(currentChar);
         }
     }

     return camelString.toString();
 }
 
 private boolean is_nan(String value) {
     return (!value.equals(value));
 }

public static void main(String[] args) {

		String CSVFile = "files/coursework2/solution/IN3067-INM713_coursework_data_pizza_500.csv";
		//Format
    	//city    city_ascii    lat    lng    country    iso2    iso3    admin_name    capital    population
    	Map<String, Integer> column_index = new HashMap<String, Integer>();
    	column_index.put("name", 0);
    	column_index.put("address", 1);
    	column_index.put("city", 2);
    	column_index.put("country", 3);
    	column_index.put("postcode", 4);
    	column_index.put("state", 5);
    	column_index.put("categories", 6);
    	column_index.put("menu item", 7);
    	column_index.put("item value", 8);
    	column_index.put("currency", 9);
    	column_index.put("item description", 10);
  	
    	try {
			CourseWork2 solution = new CourseWork2(CSVFile, column_index);
		
			String task;
			task = "taskRDF";
			//task = "task2";
			 boolean useExternalURI = solution.lookupExternalURI;
			//Create RDF triples
			if (!useExternalURI) {
				solution.CovertCSVToRDF();  //Fresh entity URIs
				//Graph with only data
				//solution.saveGraph(solution.model, CSVFile.replace(".csv", "-"+task)+"-only-data.ttl");
				solution.saveGraph(solution.model, CSVFile.replace(".csv", "-"+task)+"-with-ontology.ttl");
				
			}
		    else {
		    	solution.CovertCSVToRDF();  //Reusing URIs from DBPedia
				    
			//Graph for GoogleKG data
			//solution.saveGraph(solution.model, CSVFile.replace(".csv", "-"+task)+"-with-ontology-google.ttl");
		//	solution.saveGraph(solution.model, CSVFile.replace(".csv", "-"+task)+"-only-data-Google.ttl");
			
			//Graph for WikiKG.....
			solution.saveGraph(solution.model, CSVFile.replace(".csv", "-"+task)+"-only-data-Wiki.ttl");
						
		    }
			//solution.performReasoning("files/coursework2/pizza-restaurants-ontology.ttl");
			
				    
			//#Graph with ontology triples and entailed triples
			//solution.saveGraph(solution.inf_model, CSVFile.replace(".csv", "-"+task)+"-reasoning.ttl");
				    
			
		
		} catch (Exception e) {			
			e.printStackTrace();
		}
   	 		
	}

}
