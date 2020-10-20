package addedremoved;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.syntax.ElementGroup;

public class ConstructGenerator {
	
	private HashMap<String,ArrayList<Triple>> allTriple = new  HashMap<String,ArrayList<Triple>>();
	private String defaulGraph="";
	public ConstructGenerator(String defaulGraph) {
		this.defaulGraph=defaulGraph;
	}
	public ConstructGenerator() {
		defaulGraph=null;
	}
	
	public ConstructGenerator(List<Quad> quadList) {
		for(Quad q :quadList) {
			this.add(q.getGraph().getURI(),q.asTriple());
		}
	}
	
	public void add(String graph,Triple t) {
		if(allTriple.containsKey(graph)) {
			allTriple.get(graph).add(t);
		}else {
			ArrayList<Triple> new_List = new ArrayList<Triple>();
			new_List.add(t);
			allTriple.put(graph, new_List);
		}
	}
	
	public void add(Triple t) {
		add(this.defaulGraph, t);
	}
	public String getConstruct() {
		String sparql = "CONSTRUCT ";
		String where = " WHERE { \n";
		boolean firstGraph = true;
		ElementGroup list = new ElementGroup();
		for (String graph : allTriple.keySet()) {
			for(Triple triple :allTriple.get(graph)) {
				//sparql+=triple + ".\n";
				list.addTriplePattern(triple);
			}	
			if(firstGraph) {
				firstGraph=false;
				where+="{GRAPH <"+ graph + "> {?s ?p ?o}}\n";
			}else {

				where+="UNION{GRAPH <"+ graph + "> {?s ?p ?o}}\n";
			}
		}
		sparql+=list.toString() + where +"}";
		
		return sparql;
	}
	
	
}