package addedremoved;


import java.util.ArrayList;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import com.google.gson.JsonObject;

import connector.SparqlRequest;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import model.EndPoint;
import model.SparqlObj;
import model.TestMetric;

public class AddedRemovedGenerator {
	
	
			public static SparqlRequest generateInsertUpdate(SparqlRequest originalUpdate,ArrayList<UpdateConstruct> contructsList) throws Exception {

				
				if(contructsList.size()<=0) {
					return null;
				}
				
				
				String insert = "INSERT DATA  {";
			
				boolean needInsert = false;
				for (UpdateConstruct contruct : contructsList) {
					if(contruct.needInsert()) {						
						if(contruct.getAddedGraph()==null) {
							throw new Exception("Miss graph for generate Insert update.");
						}
						needInsert=true;						
						insert+="\nGRAPH<"+ contruct.getAddedGraph()+ "> {\n";
						for (Bindings triple : contruct.getAdded().getBindings()) {					
							
							try {
								//System.out.println("triple-->"+tripleToString(triple)); //ok
								String temp = tripleToString(triple);
								if(temp!=null) {
									insert+=temp+"\n";
								}
							} catch (SEPABindingsException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						insert+="}";
					}//else ignore
					
				}
			
				insert+="}";
				
				if(needInsert) {
					SparqlObj sparql= originalUpdate.getSparql();	
					sparql.setSparql(insert);			
					return new SparqlRequest(sparql,originalUpdate.getEndPointHost());
				}else {
					return null;
				}
			
			
			}	
			

			public static SparqlRequest generateDeleteUpdate(SparqlRequest originalUpdate,ArrayList<UpdateConstruct> contructsList) throws Exception {
				
				if(contructsList.size()<=0) {
					return null;
				}
				
				String delete = "DELETE DATA {";
				boolean needDelete = false;
				for (UpdateConstruct contruct : contructsList) {
					
					if(contruct.needDelete()) {						
						if(contruct.getRemovedGraph()==null) {
							throw new Exception("Miss graph for generate Delete update.");
						}
						needDelete=true;						
						delete+="\nGRAPH<"+ contruct.getRemovedGraph()+ "> \n{\n";
						for (Bindings triple : contruct.getRemoved().getBindings()) {					
							
							try {
								//System.out.println("triple-->"+tripleToString(triple)); //ok
								String temp = tripleToString(triple);
								if(temp!=null) {
									delete+=temp+"\n";
								}
							} catch (SEPABindingsException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						delete+="}";
					}//else ignore
					
				}
				delete+="}";
				
				if(needDelete) {
					SparqlObj sparql= originalUpdate.getSparql();
					sparql.setSparql(delete);				
					return new SparqlRequest(sparql,originalUpdate.getEndPointHost());
				}else {
					return null;
				}
			
			
			}
			
			public static SparqlRequest generateDeleteUpdate_deprecate2(SparqlRequest originalUpdate,UpdateConstruct c) throws Exception {
				
				
				if(c.getRemovedGraph()==null) {
					throw new Exception("Miss graph for generate Delete update.");
				}
				
				SparqlObj sparql= originalUpdate.getSparql();
				
				String delete = "DELETE WHERE { GRAPH  "+ c.getRemovedGraph()+ " \n{\n";
				
				for (Bindings triple : c.getRemoved().getBindings()) {					
				
					try {
						//System.out.println("triple-->"+tripleToString(triple)); //ok
						String temp = tripleToString(triple);
						if(temp!=null) {
							delete+=temp+"\n";
						}
					} catch (SEPABindingsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				delete+=" } }";
				sparql.setSparql(delete);
				
			
				return new SparqlRequest(sparql,originalUpdate.getEndPointHost());
			
			}
			
			public static SparqlRequest generateDeleteUpdate_Deprecate(SparqlRequest originalUpdate,UpdateConstruct c) throws Exception {
				
		
				if(c.getRemovedGraph()==null) {
					throw new Exception("Miss graph for generate Delete update.");
				}
				
				SparqlObj sparql= originalUpdate.getSparql();
				
				String delete = "DELETE DATA FROM "+ c.getRemovedGraph()+ " \n{\n";
				
				for (Bindings triple : c.getRemoved().getBindings()) {					
				
					try {
						//System.out.println("triple-->"+tripleToString(triple)); //ok
						String temp = tripleToString(triple);
						if(temp!=null) {
							delete+=temp+"\n";
						}
					} catch (SEPABindingsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				delete+=" }";
				sparql.setSparql(delete);
				
			
				return new SparqlRequest(sparql,originalUpdate.getEndPointHost());
			
			}
			
			public static ArrayList<UpdateConstruct> getAddedRemovedFrom(SparqlRequest req,ArrayList<TestMetric> m) {
				try {
					EndPoint endPointforQuery= req.getEndPointHost();
					endPointforQuery.setPath("/query");
					return GetAddedRemovedTriples(req.getSparql(),endPointforQuery, m);
					
				} catch (SEPASecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SEPABindingsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new ArrayList<UpdateConstruct>();
			
			}
			
			
			private static boolean isBindingPresent(Bindings bindings,SparqlObj sparql, EndPoint ep) throws SEPABindingsException, SEPASecurityException {
			
				Triple t = bindingToTriple(bindings);

				Query ask = new Query();
				ask.setQueryAskType();

				ElementTriplesBlock block = new ElementTriplesBlock();
				block.addTriple(t);
				ask.setQueryPattern(block);

				String askq = ask.serialize();
				SparqlObj askSparql= sparql;
				askSparql.setSparql(askq);
				SparqlRequest askquery = new SparqlRequest(askSparql,ep);

				BindingsResults isPresentResult = ((QueryResponse)askquery.execute()).getBindingsResults();
				return isPresentResult.toJson().get("boolean").getAsBoolean();
			}

			private static Triple bindingToTriple(Bindings bindings) throws SEPABindingsException{
				String subject = bindings.getValue("s");
				String predicate = bindings.getValue("p");
				String object = bindings.getValue("o");			
				
				Node s = bindings.isBNode("s") ? NodeFactory.createBlankNode(subject) : NodeFactory.createURI(subject);
				Node p = bindings.isBNode("p") ? NodeFactory.createBlankNode(predicate) : NodeFactory.createURI(predicate);

				Node o = null;
				if(!bindings.isBNode("o")){
					o = bindings.isURI("o") ? NodeFactory.createURI(object) : NodeFactory.createLiteral(object);
				}else{
					o = NodeFactory.createBlankNode(object);
				}

				return new Triple(s,p,o);
			}

	
			
			private static 	ArrayList<UpdateConstruct>  GetAddedRemovedTriples(SparqlObj sparql, EndPoint ep, ArrayList<TestMetric> m) throws SEPASecurityException, SEPABindingsException {
				TestMetric tm1 = new TestMetric("Constructs");	
				
				tm1.start();
				//long start = Timings.getTime();
				SPARQLAnalyzer sa = new SPARQLAnalyzer(sparql.getSparqlString());
				ArrayList<UpdateConstruct> constructsList = sa.getConstructs();
				//per ogni grafo  (per ogni grafo)
				for (UpdateConstruct constructs : constructsList) {
					//System.out.println("--------->"+constructs.getAddedGraph());//ok

					if(!constructs.isSkipConstruct()) {
						
						String dc = constructs.getDeleteConstruct();
						if (dc.length() > 0) {		
							//System.out.println("DC-->"+dc+"\n\n");		
							SparqlObj getRemovedSparql =sparql; // sparql.clone();
							getRemovedSparql.setSparql(dc);
							constructs.setRemoved(((QueryResponse) new SparqlRequest(getRemovedSparql,ep).execute()).getBindingsResults());
						}else {
							constructs.setRemoved( new BindingsResults(new JsonObject()));
						}

						String ac = constructs.getInsertConstruct();
						if (ac.length() > 0) {
							//System.out.println("AC-->"+ac+"\n\n");
							SparqlObj getAddedSparql =sparql ;// sparql.clone();
							getAddedSparql.setSparql(ac);
							// System.out.println("-->"+new SparqlRequest(getAddedSparql,ep).execute().toString());
							constructs.setAdded(((QueryResponse) new SparqlRequest(getAddedSparql,ep).execute()).getBindingsResults());
							
						}else {
							constructs.setAdded( new BindingsResults(new JsonObject()));
						}
						
					}else {
						constructs.setAdded(constructs.getAdded());
						constructs.setRemoved(constructs.getRemoved());
					}	
				}
				
					
				tm1.stop();				
				//nuovamente per ogni construct (per ogni grafo)				
				TestMetric tm2 = new TestMetric("ASKs");
				tm2.start();
				for (UpdateConstruct constructs : constructsList) {
					for(Bindings bindings : constructs.getAdded().getBindings()){
						boolean isPresent = isBindingPresent(bindings,sparql,ep);
						if(isPresent){
							constructs.removeBingingFromAddedList(bindings);
						}
					}
					for(Bindings bindings : constructs.getRemoved().getBindings()){
						boolean isPresent = isBindingPresent(bindings,sparql,ep);
						if(!isPresent){
							constructs.removeBingingFromRemovedList(bindings);
						}
					}
				
				}
				tm2.stop();
				
				m.add(tm1);
				m.add(tm2);
				return constructsList;
			}
			

			
			public static String nuplaToString(Bindings triple) throws SEPABindingsException {
				if(triple.getVariables().size()<1){
					return null;
				}
				String tripl = "";
				for (String var : triple.getVariables()) {
					tripl+=triple.isURI(var)? "<"+triple.getValue(var)+">": "\""+triple.getValue(var)+"\"" ;
					tripl+= " ";
				}				
				tripl+=" .";
				return tripl;
				
			} 
			
			public static String tripleToString(Bindings triple) throws SEPABindingsException {
				if(triple.getVariables().contains("s") && triple.getVariables().contains("p")  && triple.getVariables().contains("o") ) {
					String s =triple.isURI("s")? "<"+triple.getValue("s")+">": "\""+triple.getValue("s")+"\"";
					String p =triple.isURI("p")? "<"+triple.getValue("p")+">": "\""+triple.getValue("p")+"\"";
					String o =triple.isURI("o")? "<"+triple.getValue("o")+">": "\""+triple.getValue("o")+"\"";
					return s+ " "+ p + " " + o+ " .";
				}else {
					return null;
				}
				
			} 
}

