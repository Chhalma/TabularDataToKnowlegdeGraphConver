# Tabular Data to Knowledge Graph - Project Overview

This project aims to transform tabular data into RDF triples using Java. The data used for this task is sourced from a Kaggle dataset containing information about pizza restaurants and the pizzas they sell. The transformation process is guided by the provided `cw_onto` ontology(pizza-restaurants-ontology.ttl).

## Requirements

### Task RDF
The primary task involves transforming the provided `cw_data` into RDF triples. 
### Subtask RDF.0
Before starting the transformation, extend `cw_data` with new entries in the CSV file.

### Subtask RDF.1
URI generation is crucial to ensure unique identifiers for created individuals.

### Subtask RDF.2
Generate RDF triples for the extended `cw_data` and evaluate through blind queries.

### Subtask RDF.3
Reuse URIs from state-of-the-art knowledge graphs (Google KG and Wikidata KG) for city, country, and state information.

### Subtask RDF.4
Perform reasoning with `cw_onto` and the generated RDF data, saving the extended graph in Turtle format.

### Subtask RDF.5 (Optional)
Exploit an external Knowledge Graph for disambiguation and error resolution.

### SPARQL and Reasoning (Task SPARQL)
Write meaningful SPARQL queries and execute them over the extended graph after reasoning with both `cw_onto` and the generated data in Task RDF.

### Subtask SPARQL.1
Create a query with at least two triple patterns and a FILTER.

### Subtask SPARQL.2
Create a query that uses at least three triple patterns, a FILTER, and a function.

### Subtask SPARQL.3
Create a query that uses the Union graph pattern and SPARQL 1.1 negation.

### Subtask SPARQL.4
Create a query that groups results, uses aggregates, and a filter over the aggregates.

### Subtask SPARQL.5
Create a query that groups results, uses aggregates, filters the results (over the aggregates), and orders the results according to two variables.

### Ontology Alignment (Task OA)
Perform a basic alignment between the provided `pizza.owl` ontology and `cw_onto`.

### Subtask OA.1
Compute equivalences between the entities of `pizza.owl` and `cw_onto` ontologies.

### Subtask OA.2
Compute the precision and recall of mappings against the given reference mappings.

### Subtask OA.3
Perform reasoning with all the following sources in a single graph or RDF model.

### Subtask OA.4
Create a valid SPARQL query with at least two triple patterns that uses the vocabulary of `pizza.owl` and retrieves results from the generated data.

### Ontology Embeddings (Task Vector)
Create embeddings capturing the rich semantics of `cw_onto` and the generated data.

### Subtask Vector.1
Run OWL2Vec* with `cw_onto` and the generated data from Task RDF.

### Subtask Vector.2
Select pairs of entities and discuss the similarity of their vectors.

### Subtask Vector.3 (Optional)
Solve Subtask OA.1 using similarity among ontology embeddings calculated with OWL2Vec*.

## Project Structure


## Project Structure Overview

- **com.city.cw.swt.rdf**: Package containing code for RDF generation.
  - `RDFMain.java`: Generates the RDF file for the tabular data.
  - `WikidataURIRDFMain.java`: Generates the RDF file using Wikidata KG for City, State, and Country.
  - `GoogleURIRDFMain.java`: Generates the RDF file using Google KG for City, State, and Country.

- **com.city.cw.swt.sparql**: Package containing code for executing SPARQL queries.
  - `SPARQLQueryMain.java`: Executes SPARQL queries and saves the results.

- **com.city.cw.swt.ontologyalignment**: Package containing classes for ontology alignment tasks.The main classes are as below
  - `OntologyEquivalenceCalculator.java`: Computes equivalences between entities of pizza.owl and cw_onto ontologies.
  - `PrecisionRecallCalculation.java`: Computes precision and recall of mappings against reference mappings.
  - `SubTask_OA_3.java`: Combines ontologies, computed alignment, and generated data for reasoning.
  - `SubTask_OA_4.java`: Executes SPARQL queries using the vocabulary of pizza.owl and retrieves results from generated data.

- **com.city.cw.swt.ontologyEmbeding**: Package for embedding tasks and to calculate similarity for 6 pairs of entities.
  - `Owl2VecEmbedding.java`: The main executable file to do the  Embeding part and calculate similarity for 6 pairs of entities.
  - `MergeCWOnto_Data.java`: `Creates pizza-restaurants-ontology.owl` file which was used in python to generate `document_sentences.txt`.
 

- **files**: Folder containing input files such as `Pizza.owl`, `pizza-restaurants-ontology.owl`, `IN3067-INM713_coursework_data_pizza_500.csv`, `referencemappings-pizza.ttl`.

- **output**: Folder to store final output for each task.
  - `rdf`: Folder to store RDF output.
  - `queryResult`: Folder to store SPARQL query results.
  - `embedding`: Folder to store results of embedding tasks.

## Usage

1. Clone this repository.
2. Navigate to the respective package containing the task you want to run.
3. Ensure all input files are placed in the `files` directory.
4. Run the main classes as listed above
5. Check the `output` directory for the results of each task.

## Contributors

- Pravija Sandeep 
- Chhalma Chhaya 


## Acknowledgments

- Special thanks to Dr Jimenez Ruiz, Ernesto for guidance and support throughout the project.
- We used some of provided resources during the lab sessions for example I_Sub.java, LookupService.java, GoogleKGLookup.java , WikidataLookup.java, PrecisionRecallCalculation.java and Owl2VecEmbedding.java.

