package addedremoved;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import com.google.gson.JsonObject;

import addedremoved.ask.AsksAsSelectExistsList;
import addedremoved.ask.AsksAsSelectGraphAsVar;
import addedremoved.ask.IAsk;
import addedremoved.construct.SPARQLAnalyzer;
import connector.SparqlRequest;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPASecurityException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import it.unibo.arces.wot.sepa.commons.sparql.BindingsResults;
import model.EndPoint;
import model.SparqlObj;
import model.TestMetric;
import support.Cloner;

public class AddedRemovedManager {
	
	
			public static SparqlRequest generateInsertUpdate(SparqlRequest originalUpdate,ArrayList<UpdateExtractedData> contructsList) throws Exception {

				
				if(contructsList.size()<=0) {
					return null;
				}
				
				
				String insert = "INSERT DATA  {";
			
				boolean needInsert = false;
				for (UpdateExtractedData contruct : contructsList) {
					if(contruct.needInsert()) {						
						if(contruct.getAddedGraph()==null) {
							throw new Exception("Miss graph for generate Insert update.");
						}
						needInsert=true;						
						insert+="\nGRAPH<"+ contruct.getAddedGraph()+ "> {\n";
						for (Bindings triple : contruct.getAdded().getBindings()) {					
							
							try {
								//System.out.println("triple-->"+tripleToString(triple)); //ok
								String temp = TripleConverter.tripleToString(triple);
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
					SparqlObj sparql= originalUpdate.getSparql().clone();	
					sparql.setSparql(insert);			
					EndPoint ep =originalUpdate.getEndPointHost().clone();
					ep.setPath("/update");
					return new SparqlRequest(sparql,ep);
				}else {
					return null;
				}
			
			
			}	
			

			public static SparqlRequest generateDeleteUpdate(SparqlRequest originalUpdate,ArrayList<UpdateExtractedData> contructsList) throws Exception {
				
				if(contructsList.size()<=0) {
					return null;
				}
				
				String delete = "DELETE DATA {";
				boolean needDelete = false;
				for (UpdateExtractedData contruct : contructsList) {
					
					if(contruct.needDelete()) {						
						if(contruct.getRemovedGraph()==null) {
							throw new Exception("Miss graph for generate Delete update.");
						}
						needDelete=true;						
						delete+="\nGRAPH<"+ contruct.getRemovedGraph()+ "> \n{\n";
						for (Bindings triple : contruct.getRemoved().getBindings()) {					
							
							try {
								//System.out.println("triple-->"+tripleToString(triple)); //ok
								String temp = TripleConverter.tripleToString(triple);
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
					SparqlObj sparql= originalUpdate.getSparql().clone();
					sparql.setSparql(delete);		
					EndPoint ep =originalUpdate.getEndPointHost().clone();
					ep.setPath("/update");
					return new SparqlRequest(sparql,ep);
				}else {
					return null;
				}
			
			
			}
		
			
		
			
			public static ArrayList<UpdateExtractedData> getAddedRemovedFrom(SparqlRequest req,ArrayList<TestMetric> m) throws SEPABindingsException, CloneNotSupportedException {
				
					EndPoint endPointforQuery= req.getEndPointHost().clone();
					endPointforQuery.setPath("/query");
					return getAddedRemovedFrom(req.getSparql().clone(),endPointforQuery, m);	
			
			}
	
			
			private static ArrayList<UpdateExtractedData>  getAddedRemovedFrom(SparqlObj sparql, EndPoint ep, ArrayList<TestMetric> m) throws SEPABindingsException, CloneNotSupportedException {
				TestMetric tm1 = new TestMetric("Constructs");	
				
				tm1.start();
				//long start = Timings.getTime();
				SPARQLAnalyzer sa = new SPARQLAnalyzer(sparql.getSparqlString());
				ArrayList<UpdateExtractedData> constructsList = sa.getConstructs();
				//per ogni grafo  (per ogni grafo)
				for (UpdateExtractedData constructs : constructsList) {
					//System.out.println("--------->"+constructs.getAddedGraph());//ok

					if(!constructs.isSkipConstruct()) {
						
						String dc = constructs.getDeleteConstruct();
						if (dc.length() > 0) {		
							//System.out.println("DC-->"+dc+"\n\n");		
							SparqlObj getRemovedSparql = sparql.clone();
							getRemovedSparql.setSparql(dc);
							constructs.setRemoved(((QueryResponse) new SparqlRequest(getRemovedSparql,ep).execute()).getBindingsResults());
						}else {
							constructs.setRemoved( new BindingsResults(new JsonObject()));
						}

						String ac = constructs.getInsertConstruct();
						if (ac.length() > 0) {
							//System.out.println("AC-->"+ac+"\n\n");
							SparqlObj getAddedSparql = sparql.clone();
							getAddedSparql.setSparql(ac);
							// System.out.println("-->"+new SparqlRequest(getAddedSparql,ep).execute().toString());
							constructs.setAdded(((QueryResponse) new SparqlRequest(getAddedSparql,ep).execute()).getBindingsResults());							
						}else {
							constructs.setAdded( new BindingsResults(new JsonObject()));
						}
						
					}
				}			
				tm1.stop();	
				
	
//				TestMetric tm3 = new TestMetric("ASKsAsSelectGraphAsVar");				
//				tm3.start();
//				IAsk asks2= new AsksAsSelectGraphAsVar(Cloner.deepCopy(constructsList), sparql, ep);
//				constructsList=asks2.filter();
//				tm3.stop();
//				
//				TestMetric tm2 = new TestMetric("ASKsAsSelectExistsList");
//				tm2.start();
//				IAsk asks= new AsksAsSelectExistsList(constructsList, sparql, ep);
//				constructsList=asks.filter();
//				tm2.stop();
				
				TestMetric tm2 = new TestMetric("ASKs");
				tm2.start();
				IAsk asks= new AsksAsSelectExistsList(constructsList, sparql.clone(), ep);
				constructsList=asks.filter();
				tm2.stop();
				
				m.add(tm1);
				m.add(tm2);
//				m.add(tm3);
				return constructsList;
			}
			

			
		
		
}
