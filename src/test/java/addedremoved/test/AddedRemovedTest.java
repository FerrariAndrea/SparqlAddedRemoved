package addedremoved.test;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import addedremoved.AddedRemovedGenerator;
import connector.IRequestFactory;
import connector.RequestFactory;
import connector.RequestFactory.RequestName;
import connector.SparqlRequest;
import core.Inspector;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPABindingsException;
import it.unibo.arces.wot.sepa.commons.response.QueryResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparql.Bindings;
import model.TestMetric;
import model.UpdateConstruct;
import support.Metrics;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddedRemovedTest {

	private static IRequestFactory factory = null;
	private static Metrics metrics = null;
	@BeforeClass
	public static void init() {
		//instanzio la fattoria di richieste
		factory=RequestFactory.getInstance();
		//e il raccoglitore delle metriche
		metrics=Metrics.getInstance();
	}

	
	@Test // (timeout = 5000)
	public void test_Q2() {

		SparqlRequest update_for_Q2=(SparqlRequest)factory.getRequestByName(RequestName.UPDATE_FOR_Q2.toString());
		SparqlRequest query_Q2=(SparqlRequest)factory.getRequestByName(RequestName.QUERY2.toString());
		SparqlRequest roolback_for_Q2=(SparqlRequest)factory.getRequestByName(RequestName.ROLLBACK_FOR_Q2.toString());
		SparqlRequest deleteUpdate=null;	
		SparqlRequest insertUpdate=null;
		ArrayList<TestMetric> phases = new ArrayList<TestMetric>();
		
		//----------------------------------Phase 1
		TestMetric Phase1 = new TestMetric("Added removed extraction and generation of updates (insert and delete)");		
		
		Phase1.start();
		UpdateConstruct constructs = AddedRemovedGenerator.getAddedRemovedFrom(update_for_Q2.clone());
		if(constructs.needDelete()) {
			try {
				deleteUpdate =AddedRemovedGenerator.generateDeleteUpdate(update_for_Q2.clone(),constructs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(constructs.needInsert()) {
			try {
				insertUpdate =AddedRemovedGenerator.generateInsertUpdate(update_for_Q2.clone(),constructs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Phase1.stop();
		phases.add(Phase1);
	    assertFalse("#)  Phase1",constructs==null || (deleteUpdate==null && insertUpdate==null) );
	    
		//----------------------------------Phase 2	    
		TestMetric Phase2 = new TestMetric("Execution query");
		
		Phase2.start();
		Response pre_ris_query = query_Q2.execute();
		Phase2.stop();
		phases.add(Phase2);
		assertFalse("#)  Phase2",pre_ris_query.isError());
		
		//----------------------------------Phase 3	    
		TestMetric Phase3 = new TestMetric("Execution normal update");
		Phase3.start();
		Response ris_update = update_for_Q2.execute();
		Phase3.stop();
		phases.add(Phase3);
		assertFalse("#)  Phase3",ris_update.isError());
		
		//----------------------------------Phase 4	    
		TestMetric Phase4 = new TestMetric("Execution Query N�2 1/2");
		
		Phase4.start();
		QueryResponse ris_Query = (QueryResponse)query_Q2.execute();
		Phase4.stop();
		phases.add(Phase4);
		assertFalse("#)  Phase4",ris_Query.isError());
		

		//----------------------------------Phase 5	    
		TestMetric Phase5 = new TestMetric("Execution RoolBack Update");
		
		Phase5.start();
		Response ris_Roolback =roolback_for_Q2.execute();
		Phase5.stop();
		phases.add(Phase5);
		assertFalse("#)  Phase5",ris_Roolback.isError());
	
		
		//----------------------------------Phase 6	    
		TestMetric Phase6 = new TestMetric("Execution insert and delete");
		Response ris_insert =null;
		Response ris_delete =null;
		
		Phase6.start();
		if(deleteUpdate!=null) {
			ris_delete =deleteUpdate.execute();
		}
		if(insertUpdate!=null) {
			ris_insert =insertUpdate.execute();
		}
		Phase6.stop();
		phases.add(Phase6);
		if(insertUpdate!=null){
			assertFalse("#)  Phase6 insert success",ris_insert.isError());
		}
		if(deleteUpdate!=null){
			assertFalse("#)  Phase6 delete success",ris_delete.isError());
		}
		
		if(ris_insert==null && ris_delete==null) {
			System.out.println("Warning both insert and delete updates are void!");
		}
		
		//----------------------------------Phase 7	    
		TestMetric Phase7 = new TestMetric("Execution Query N�2 2/2");
		
		
		Phase7.start();
		QueryResponse ris_Query_2 = (QueryResponse)query_Q2.execute();
		Phase7.stop();
		phases.add(Phase7);
		assertFalse("#)  Phase7",ris_Query_2.isError());
		
		

		
		//----------------------------------Phase 8	    
		TestMetric Phase8 = new TestMetric("Re-Execution RoolBack Update");
		
		Phase8.start();
		ris_Roolback =roolback_for_Q2.execute();
		Phase8.stop();
		phases.add(Phase8);
		assertFalse("#)  Phase8",ris_Query.isError());
		
		//----------------------------------Phase 9	
		assertTrue("#)  Phase9 Querys resutl (need be equasl)",Inspector.areEq(ris_Query_2.getBindingsResults(),ris_Query.getBindingsResults()));
				
		
		printTestResult(
				"TEST 1",
				phases,
				update_for_Q2,
				insertUpdate,
				deleteUpdate,
				(QueryResponse)pre_ris_query,
				ris_Query,
				ris_Query_2
				);
		
	}

	@Test // (timeout = 5000)
	public void test_Q3() {

		SparqlRequest update_for_Q3=(SparqlRequest)factory.getRequestByName(RequestName.UPDATE_FOR_Q3.toString());
		SparqlRequest query_Q3=(SparqlRequest)factory.getRequestByName(RequestName.QUERY3.toString());
		SparqlRequest deleteUpdate=null;	
		SparqlRequest insertUpdate=null;
		ArrayList<TestMetric> phases = new ArrayList<TestMetric>();
		
		//----------------------------------Phase 1
		TestMetric Phase1 = new TestMetric("Added removed extraction and generation of updates (insert and delete)");		
		
		Phase1.start();
		UpdateConstruct constructs = AddedRemovedGenerator.getAddedRemovedFrom(update_for_Q3.clone());
		if(constructs.needDelete()) {
			try {
				deleteUpdate =AddedRemovedGenerator.generateDeleteUpdate(update_for_Q3.clone(),constructs);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(constructs.needInsert()) {
			try {
				insertUpdate =AddedRemovedGenerator.generateInsertUpdate(update_for_Q3.clone(),constructs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Phase1.stop();
		phases.add(Phase1);
	    assertFalse("#)  Phase1",constructs==null || (deleteUpdate==null && insertUpdate==null) );
	    
		//----------------------------------Phase 2	    
		TestMetric Phase2 = new TestMetric("Execution query");
		
		Phase2.start();
		Response pre_ris_query = query_Q3.execute();
		Phase2.stop();
		phases.add(Phase2);
		assertFalse("#)  Phase2",pre_ris_query.isError());
		
		//----------------------------------build roolback
		
		String oldTriples ="";
		for (Bindings binds : constructs.getRemoved().getBindings()) {
			try {
				String tmep = AddedRemovedGenerator.tripleToString(binds);
				if(tmep!=null) {
					oldTriples+=AddedRemovedGenerator.tripleToString(binds)+"\n";
				}			
			} catch (SEPABindingsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SparqlRequest roolback_for_Q3=null;
		try {
			roolback_for_Q3 = (SparqlRequest)((RequestFactory)factory).buildRequestByName(RequestName.ROLLBACK_FOR_Q3.toString(),oldTriples);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue("ROLLBACK_FOR_Q3 not found!",false);
		}
				
		//----------------------------------Phase 3	    
		TestMetric Phase3 = new TestMetric("Execution normal update");
		Phase3.start();
		Response ris_update = update_for_Q3.execute();
		Phase3.stop();
		phases.add(Phase3);
		assertFalse("#)  Phase3",ris_update.isError());
		
		//----------------------------------Phase 4	    
		TestMetric Phase4 = new TestMetric("Execution Query N�2 1/2");
		
		Phase4.start();
		QueryResponse ris_Query = (QueryResponse)query_Q3.execute();
		Phase4.stop();
		phases.add(Phase4);		
		assertFalse("#)  Phase4",ris_Query.isError());
		

		//----------------------------------Phase 5	    
		TestMetric Phase5 = new TestMetric("Execution RoolBack Update");
		System.out.println("roolback->"+roolback_for_Q3.getSparql().getSparqlString());
		Phase5.start();
		Response ris_Roolback =roolback_for_Q3.execute();
		Phase5.stop();
		phases.add(Phase5);	
		assertFalse("#)  Phase5",ris_Roolback.isError());
	
		
		//----------------------------------Phase 6	    
		TestMetric Phase6 = new TestMetric("Execution insert and delete");
		Response ris_insert =null;
		Response ris_delete =null;
		
		System.out.println("deleteUpdate: "+deleteUpdate.getSparql().getSparqlString());
		System.out.println("insert: "+insertUpdate.getSparql().getSparqlString());
		
		Phase6.start();		
		if(deleteUpdate!=null) {
			ris_delete =deleteUpdate.execute();
		}
		if(insertUpdate!=null) {
			ris_insert =insertUpdate.execute();
		}
		Phase6.stop();
		phases.add(Phase6);	
		if(insertUpdate!=null){
			assertFalse("#)  Phase6 insert success",ris_insert.isError());
		}
		if(deleteUpdate!=null){
			assertFalse("#)  Phase6 delete success",ris_delete.isError());
		}
		
		if(ris_insert==null && ris_delete==null) {
			System.out.println("Warning both insert and delete updates are void!");
		}
		
		//----------------------------------Phase 7	    
		TestMetric Phase7 = new TestMetric("Execution Query N�2 2/2");
		
		
		Phase7.start();
		QueryResponse ris_Query_2 = (QueryResponse)query_Q3.execute();
		Phase7.stop();
		phases.add(Phase7);	
		assertFalse("#)  Phase7",ris_Query_2.isError());
		
		//----------------------------------Phase 8	    
		TestMetric Phase8 = new TestMetric("Re-Execution RoolBack Update");
			
		Phase8.start();
		ris_Roolback =roolback_for_Q3.execute();
		Phase8.stop();
		phases.add(Phase8);	
		assertFalse("#)  Phase8",ris_Query.isError());
		
		//----------------------------------Phase 9	
		assertTrue("#)  Phase9 Querys resutl (need be equasl)",Inspector.areEq(ris_Query_2.getBindingsResults(),ris_Query.getBindingsResults()));
		
		printTestResult(
				"TEST 2",
				phases,
				update_for_Q3,
				insertUpdate,
				deleteUpdate,
				(QueryResponse)pre_ris_query,
				ris_Query,
				ris_Query_2
				);
		
		
	}
	
	
	
	private void printTestResult(String testName,ArrayList<TestMetric> phases, SparqlRequest normalUpdate,SparqlRequest insertUpdate,SparqlRequest deleteUpdate,QueryResponse ris_pre_up_Query,QueryResponse ris_Query, QueryResponse ris_Query_2 ) {
		System.out.println("--------------------------------------------------------------"+testName);
		System.out.println("---------------------------------------------------------");
		System.out.println("---------------------Normal update------------------------");
		System.out.println("---------------------------------------------------------");		
		System.out.println(normalUpdate.getSparql().getSparqlString());
		System.out.println("---------------------------------------------------------");
		System.out.println("-----------------Update for insert data------------------");
		System.out.println("---------------------------------------------------------");
		if(insertUpdate==null) {
			System.out.println("No insert update needed!");
		}else {
			System.out.println(insertUpdate.getSparql().getSparqlString());
		}
		System.out.println("---------------------------------------------------------");
		System.out.println("-----------------Update for delete data------------------");
		System.out.println("---------------------------------------------------------");
		if(deleteUpdate==null) {
			System.out.println("No delete update needed!");
		}else {
			System.out.println(deleteUpdate.getSparql().getSparqlString());
		}		
		System.out.println("---------------------------------------------------------");
		System.out.println("-------------------Query before update-------------------");
		System.out.println("---------------------------------------------------------");
		System.out.println(ris_pre_up_Query.getBindingsResults().toJson().toString());
		System.out.println("---------------------------------------------------------");
		System.out.println("------------Query after normal update result-------------");
		System.out.println("---------------------------------------------------------");
		System.out.println(ris_Query.getBindingsResults().toJson().toString());
		System.out.println("---------------------------------------------------------");
		System.out.println("------------Query after insert delete updates------------");
		System.out.println("---------------------------------------------------------");
		System.out.println(ris_Query_2.getBindingsResults().toJson().toString());
		System.out.println("---------------------------------------------------------");
		System.out.println("----------------------Phases Times-----------------------");
		System.out.println("---------------------------------------------------------");
	
		for (TestMetric testMetric : phases) {
			testMetric.print();
		}
		
		System.out.println("-------------------------------------------------------------------");
		System.out.println("\n");
	}
	
	@Test // (timeout = 5000)
	public void test_Q4() {

		SparqlRequest update_for_Q4=(SparqlRequest)factory.getRequestByName(RequestName.UPDATE_FOR_Q4.toString());
		SparqlRequest roolback_for_Q4=(SparqlRequest)factory.getRequestByName(RequestName.ROLLBACK_FOR_Q4.toString());
		SparqlRequest query_Q4=(SparqlRequest)factory.getRequestByName(RequestName.QUERY4.toString());
		SparqlRequest deleteUpdate=null;	
		SparqlRequest insertUpdate=null;
		ArrayList<TestMetric> phases = new ArrayList<TestMetric>();
		
		//----------------------------------Phase 1
		TestMetric Phase1 = new TestMetric("Added removed extraction and generation of updates (insert and delete)");		
		
		Phase1.start();
		UpdateConstruct constructs = AddedRemovedGenerator.getAddedRemovedFrom(update_for_Q4.clone());
		if(constructs.needDelete()) {
			try {
				deleteUpdate =AddedRemovedGenerator.generateDeleteUpdate(update_for_Q4.clone(),constructs);
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(constructs.needInsert()) {
			try {
				insertUpdate =AddedRemovedGenerator.generateInsertUpdate(update_for_Q4.clone(),constructs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Phase1.stop();
		phases.add(Phase1);
	    assertFalse("#)  Phase1",constructs==null || (deleteUpdate==null && insertUpdate==null) );
	    
		//----------------------------------Phase 2	    
		TestMetric Phase2 = new TestMetric("Execution query");
		
		Phase2.start();
		Response pre_ris_query = query_Q4.execute();
		Phase2.stop();
		phases.add(Phase2);
		assertFalse("#)  Phase2",pre_ris_query.isError());
		
		
				
		//----------------------------------Phase 3	    
		TestMetric Phase3 = new TestMetric("Execution normal update");
		Phase3.start();
		Response ris_update = update_for_Q4.execute();
		Phase3.stop();
		phases.add(Phase3);
		assertFalse("#)  Phase3",ris_update.isError());
		
		//----------------------------------Phase 4	    
		TestMetric Phase4 = new TestMetric("Execution Query N�2 1/2");
		
		Phase4.start();
		QueryResponse ris_Query = (QueryResponse)query_Q4.execute();
		Phase4.stop();
		phases.add(Phase4);		
		assertFalse("#)  Phase4",ris_Query.isError());
		

		//----------------------------------Phase 5	    
		TestMetric Phase5 = new TestMetric("Execution RoolBack Update");
		System.out.println("roolback->"+roolback_for_Q4.getSparql().getSparqlString());
		Phase5.start();
		Response ris_Roolback =roolback_for_Q4.execute();
		Phase5.stop();
		phases.add(Phase5);	
		assertFalse("#)  Phase5",ris_Roolback.isError());
	
		
		//----------------------------------Phase 6	    
		TestMetric Phase6 = new TestMetric("Execution insert and delete");
		Response ris_insert =null;
		Response ris_delete =null;
		
		System.out.println("deleteUpdate: "+deleteUpdate.getSparql().getSparqlString());
		System.out.println("insert: "+insertUpdate.getSparql().getSparqlString());
		
		Phase6.start();		
		if(deleteUpdate!=null) {
			ris_delete =deleteUpdate.execute();
		}
		if(insertUpdate!=null) {
			ris_insert =insertUpdate.execute();
		}
		Phase6.stop();
		phases.add(Phase6);	
		if(insertUpdate!=null){
			assertFalse("#)  Phase6 insert success",ris_insert.isError());
		}
		if(deleteUpdate!=null){
			assertFalse("#)  Phase6 delete success",ris_delete.isError());
		}
		
		if(ris_insert==null && ris_delete==null) {
			System.out.println("Warning both insert and delete updates are void!");
		}
		
		//----------------------------------Phase 7	    
		TestMetric Phase7 = new TestMetric("Execution Query N�2 2/2");
		
		
		Phase7.start();
		QueryResponse ris_Query_2 = (QueryResponse)query_Q4.execute();
		Phase7.stop();
		phases.add(Phase7);	
		assertFalse("#)  Phase7",ris_Query_2.isError());
		
		//----------------------------------Phase 8	    
		TestMetric Phase8 = new TestMetric("Re-Execution RoolBack Update");
			
		Phase8.start();
		ris_Roolback =roolback_for_Q4.execute();
		Phase8.stop();
		phases.add(Phase8);	
		assertFalse("#)  Phase8",ris_Query.isError());
		
		//----------------------------------Phase 9	
		assertTrue("#)  Phase9 Querys resutl (need be equasl)",Inspector.areEq(ris_Query_2.getBindingsResults(),ris_Query.getBindingsResults()));
		
		printTestResult(
				"TEST 3",
				phases,
				update_for_Q4,
				insertUpdate,
				deleteUpdate,
				(QueryResponse)pre_ris_query,
				ris_Query,
				ris_Query_2
				);
		
		
	}
	
	
}
