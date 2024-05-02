package coursework2.ontologyAlignment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.FileManager;

public class SubTask_OA_3 {
	Model model;
	InfModel inf_model; 
	
    public void performReasoning(String cw_onto, String pizza_onto, String computedAlignment, String cw_data) {
        
    	//We should load the CW_Ontology first        
    	try {
            // Load the CW Ontology
            Dataset dataset = RDFDataMgr.loadDataset(cw_onto, RDFLanguages.TURTLE);
            model = dataset.getDefaultModel();
            System.out.println("Data triples from cw_onto: " + model.size());

            // Load Pizza Ontology
            RDFDataMgr.read(model, pizza_onto);

            // Load Computed Alignment
            RDFDataMgr.read(model, computedAlignment);

            // Load CW Data
            RDFDataMgr.read(model, cw_data);

            System.out.println("Data triples after loading all sources: " + model.size());

            // Perform reasoning
            Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
            inf_model = ModelFactory.createInfModel(reasoner, model);

            // Print the number of triples after reasoning
            System.out.println("Triples after reasoning: " + inf_model.size());
        } catch (Exception e) {
            e.printStackTrace();
        }    
        
        
    }
       public void saveGraph( String file_output) throws FileNotFoundException {
        
	    //SAVE/SERIALIZE GRAPH
		
	    OutputStream out = new FileOutputStream(file_output);
	    RDFDataMgr.write(out, inf_model, RDFFormat.TURTLE);	       

	}	

    public static void main(String[] args) {

		SubTask_OA_3 solution = new SubTask_OA_3();		
		
		String cw_onto = "files/coursework2/pizza-restaurants-ontology.ttl";
		String pizza_onto = "files/coursework2/pizza.ttl";
		String computedAlignment = "files/coursework2/solution/pizza-cw-equivalences.ttl"; 
		String cw_data = "files/coursework2/solution/IN3067-INM713_coursework_data_pizza_500-taskRDFonly-data.ttl";
		//String cw_data = "files/coursework2/output_p/result.ttl";
	 		    
		solution.performReasoning(cw_onto, pizza_onto, computedAlignment, cw_data);			
		try {
			solution.saveGraph( "files/coursework2/solution/cw_pizza_equivalance_data_reasoning.ttl");
			//solution.saveGraph( "files/coursework2/output_p/cw_pizza_equivalance_data_reasoning_p.ttl");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
