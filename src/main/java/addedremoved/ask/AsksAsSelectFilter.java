package addedremoved.ask;

import java.util.ArrayList;
import java.util.HashMap;

import addedremoved.BindingTag;
import addedremoved.UpdateExtractedData;
import connector.SparqlRequest;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import model.EndPoint;
import model.SparqlObj;

public class AsksAsSelectFilter implements IAsk{
	
	/*
	 * Example:
	 * 
		SELECT  ?s ?p ?o WHERE{
			GRAPH <prova3>{
				?s ?p  ?o.
			}
			FILTER( 
				?s = <s1> &&  ?p = <P> && ?o = <o1> ||
				?s = <s2> &&  ?p = <P> && ?o = <o1> 
			)
		}


	 */

	public HashMap<String,String> removedAsksAsSelect=null;
	public HashMap<String,String> addedAsksAsSelect=null;
	private ArrayList<UpdateExtractedData> ueds = new ArrayList<UpdateExtractedData> ();
	private SparqlObj sparql;
	private EndPoint endPoint;
	
	public AsksAsSelectFilter(ArrayList<UpdateExtractedData> ueds, SparqlObj sparql, EndPoint endPoint) {
		this.ueds=ueds;
		this.sparql=sparql;
		this.endPoint=endPoint;
		this.init();
	}

	protected void init() {
		for (UpdateExtractedData updateConstruct : ueds) {
			if(updateConstruct.needDelete()) {
				String deleteGraph= updateConstruct.getRemovedGraph();
				for (Bindings bind : updateConstruct.getRemoved().getBindings()) {
					putInRemoved(deleteGraph,incapsulate(bind));
				}
			}
			if(updateConstruct.needInsert()) {
				String addedGraph= updateConstruct.getAddedGraph();
				for (Bindings bind : updateConstruct.getAdded().getBindings()) {
					putInAdded(addedGraph,incapsulate(bind));
				}
			}
		}
		if(needAskSelectForRemoved()) {
			for (String graph : removedAsksAsSelect.keySet()) {
				removedAsksAsSelect.put(graph,generateSelect(graph,removedAsksAsSelect.get(graph)));
			}
		}
		if(needAskSelectForAdded()) {
			for (String graph : addedAsksAsSelect.keySet()) {
				addedAsksAsSelect.put(graph,generateSelect(graph,addedAsksAsSelect.get(graph)));
			}
		}
	}
	
	private void putInAdded(String graph,String added) {
			if(removedAsksAsSelect==null) {
				removedAsksAsSelect= new  HashMap<String,String>();
			}
			if(removedAsksAsSelect.containsKey(graph)) {
				removedAsksAsSelect.put(graph,removedAsksAsSelect.get(graph)+added);
			}else {
				removedAsksAsSelect.put(graph,added);
			}			
	}
	private void putInRemoved(String graph,String added) {
		if(addedAsksAsSelect==null) {
			addedAsksAsSelect= new  HashMap<String,String>();
		}
		if(addedAsksAsSelect.containsKey(graph)) {
			addedAsksAsSelect.put(graph,addedAsksAsSelect.get(graph)+added);
		}else {
			addedAsksAsSelect.put(graph,added);
		}			
	}

	protected String generateSelect(String graph,String values) {
		return "SELECT ?"+BindingTag.SUBJECT.toString()
				+" ?"+BindingTag.PREDICATE.toString()
				+" ?"+BindingTag.OBJECT.toString()
				+" WHERE { GRAPH <"+graph+"> { ?"+BindingTag.SUBJECT.toString()
				+" ?"+BindingTag.PREDICATE.toString()
				+" ?"+BindingTag.OBJECT.toString()
				+" }\n FILTER (\\n\" + values+ \") }";
	}
	
	protected String incapsulate(Bindings bind ) {
		return "?"+BindingTag.SUBJECT.toString()+" == <"+bind.getValue(BindingTag.SUBJECT.toString())
				+"> && ?"+BindingTag.PREDICATE.toString()+"== <"+bind.getValue(BindingTag.PREDICATE.toString())
				+"> && ?"+BindingTag.OBJECT.toString()+" == <"+bind.getValue(BindingTag.OBJECT.toString())+">\n";
	}

	
	public HashMap<String,BindingsResults> getBindingsForRemoved()  {
		HashMap<String,BindingsResults> ris =new HashMap<String,BindingsResults>();
		for (String  graph : removedAsksAsSelect.keySet()) {
			SparqlObj askSparql= sparql;
			askSparql.setSparql(removedAsksAsSelect.get(graph));
			SparqlRequest askquery = new SparqlRequest(askSparql,endPoint);
			ris.put(graph, ((QueryResponse)askquery.execute()).getBindingsResults());
		}
		return ris;
		
		
	}
	
	
	public HashMap<String,BindingsResults> getBindingsForAdded()  {
		
		HashMap<String,BindingsResults> ris =new HashMap<String,BindingsResults>();
		for (String  graph : addedAsksAsSelect.keySet()) {
			SparqlObj askSparql= sparql;
			askSparql.setSparql(addedAsksAsSelect.get(graph));
			SparqlRequest askquery = new SparqlRequest(askSparql,endPoint);
			ris.put(graph, ((QueryResponse)askquery.execute()).getBindingsResults());
		}
		return ris;
		
	}
	
	
	public HashMap<String,BindingsResults> getReorganizedBindingsForAdded() throws SEPABindingsException  {
		
		HashMap<String,BindingsResults>  list = new HashMap<String,BindingsResults>();
		
		ArrayList<String> vars = new ArrayList<String>();
		vars.add(BindingTag.SUBJECT.toString());
		vars.add(BindingTag.PREDICATE.toString());
		vars.add(BindingTag.OBJECT.toString());
		
		if(needAskSelectForAdded()) {
//			TestMetric t= new TestMetric("");
//			t.start();
			HashMap<String,BindingsResults> result = getBindingsForAdded();
//			t.stop();
//			System.out.println("real call: "+t.getInterval());
			for(String graph : result.keySet() ) {
				for (Bindings bind : result.get(graph).getBindings()) {
					Bindings triple = new Bindings();
					triple.addBinding(BindingTag.SUBJECT.toString(), bind.getRDFTerm(BindingTag.SUBJECT.toString()));
					triple.addBinding(BindingTag.PREDICATE.toString(), bind.getRDFTerm(BindingTag.PREDICATE.toString()));
					triple.addBinding(BindingTag.OBJECT.toString(), bind.getRDFTerm(BindingTag.OBJECT.toString()));
					
					if(list.containsKey(graph)) {
						list.get(graph).add(triple);
					}else {
						ArrayList<Bindings> bindList = new ArrayList<Bindings>();
						bindList.add(triple);
						list.put(graph,new BindingsResults(vars, bindList));	
					}
				}
			}
		}
		return list;		
		
	}
	
	
	public HashMap<String,BindingsResults> getReorganizedBindingsForRemoved() throws SEPABindingsException  {
		
		HashMap<String,BindingsResults>  list = new HashMap<String,BindingsResults>();
		
		ArrayList<String> vars = new ArrayList<String>();
		vars.add(BindingTag.SUBJECT.toString());
		vars.add(BindingTag.PREDICATE.toString());
		vars.add(BindingTag.OBJECT.toString());
		
		if(needAskSelectForRemoved()) {
			HashMap<String,BindingsResults> result = getBindingsForRemoved();
			for(String graph : result.keySet() ) {
				for (Bindings bind : result.get(graph).getBindings()) {
					Bindings triple = new Bindings();
					triple.addBinding(BindingTag.SUBJECT.toString(), bind.getRDFTerm(BindingTag.SUBJECT.toString()));
					triple.addBinding(BindingTag.PREDICATE.toString(), bind.getRDFTerm(BindingTag.PREDICATE.toString()));
					triple.addBinding(BindingTag.OBJECT.toString(), bind.getRDFTerm(BindingTag.OBJECT.toString()));
					
					if(list.containsKey(graph)) {
						list.get(graph).add(triple);
					}else {
						ArrayList<Bindings> bindList = new ArrayList<Bindings>();
						bindList.add(triple);
						list.put(graph,new BindingsResults(vars, bindList));	
					}
				}
			}
		}
		return list;		
		
	}
	
	public ArrayList<UpdateExtractedData> filter() throws SEPABindingsException {
		HashMap<String,BindingsResults> alredyExistFilter  = this.getReorganizedBindingsForAdded();
		HashMap<String,BindingsResults> realRemovedEGFilter  = this.getReorganizedBindingsForRemoved();
		for (UpdateExtractedData constructs : ueds) {
			
			String graph = constructs.getAddedGraph();
			
			if(constructs.needInsert() && alredyExistFilter.containsKey(graph) ){
				constructs.removeBingingFromAddedList(alredyExistFilter.get(graph)); 

				alredyExistFilter.remove(graph);
			}	
			
			graph = constructs.getRemovedGraph();
			
			if(realRemovedEGFilter.containsKey(graph)) {
				constructs.setRemoved(realRemovedEGFilter.get(graph));
				
				realRemovedEGFilter.remove(graph);
			}else {
				constructs.clearRemoved();
			}
			
		}

		return ueds;
	}
	
	
	public boolean needAskSelectForAdded() {
		return addedAsksAsSelect!=null && addedAsksAsSelect.size()>0 ;
	}
	
	public boolean needAskSelectForRemoved() {
		return removedAsksAsSelect!=null&& removedAsksAsSelect.size()>0;
	}

	
	
}
