package org.sf.schemareader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;

import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SObjectMetas {
	
	public static enum WalkReturnCode { terminated, completed }
	
	public static class SObjectWalkerConfig {
		
		public boolean ignoreErrors = true;
		
		public int batchSize = 100;
		
		PartnerConnection connection;
	}
	
	@FunctionalInterface
	public static interface Visitor {
		/**
		 * 
		 * @param field : the field description
		 * @param object : the object description
		 * @param path : [] #array of n objects where [0] = root object description, [1]..[n-1] = relationship descriptions, [n] = field description #]
		 */
		public WalkReturnCode visit(Field field, DescribeSObjectResult object, Object[] path);
	}
	
	public static void getDescribeResults(SObjectWalkerConfig config, Consumer<Map<String, DescribeSObjectResult>> onSuccess, int timeoutSeconds) {
	    DescribeGlobalSObjectResult[] globalDescribe;
		try {
			globalDescribe = config.connection.describeGlobal().getSobjects();
		} catch (ConnectionException e) { throw new RuntimeException("failed to fetch the global describe result", e); }
		
	    List<List<String>> batches = new ArrayList();
	    for(int i = 0; i < globalDescribe.length;) {
	    	List<String> batch = new ArrayList();
    		batches.add(batch);
	    	for(int j = 0; j < config.batchSize && i < globalDescribe.length; i++, j++)
	    		batch.add(globalDescribe[i].getName());
	    }
	    
	    Map<String, DescribeSObjectResult> result = new HashMap();
	    ExecutorService executor = Executors.newCachedThreadPool();
	    batches.forEach(batch -> {
	    	executor.submit(() -> {
	    		try { 
	    			addDescribesToMap(config.connection.describeSObjects(batch.toArray(new String[batch.size()])), result);
	    		} catch(Exception e) {
	    			if(!config.ignoreErrors)
	    				throw new RuntimeException("failed to fetch sobject describe result", e);
	    			else
	    				e.printStackTrace();
	    		}
	    	});
	    });
	    
	    try {
	    	executor.shutdown();
			executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace(); 
		}
	    
	    onSuccess.accept(result);
	}
	
	public static synchronized void addDescribesToMap(DescribeSObjectResult[] describeArray, Map<String, DescribeSObjectResult> map) {
		for(DescribeSObjectResult r : describeArray)
			map.put(r.getName(), r);
	}
	
	public static WalkReturnCode shallowWalkFields(Map<String, DescribeSObjectResult> describes, Visitor visitor) {
		for(DescribeSObjectResult describe : describes.values())
			if(shallowWalkFields(describe, visitor) == WalkReturnCode.terminated) return WalkReturnCode.terminated;
		return WalkReturnCode.completed;
	}
	
	public static WalkReturnCode shallowWalkFields(DescribeSObjectResult describe, Object[] path, Visitor visitor) {
		for(Field field : describe.getFields()) {
			Object[] subPath = ArrayUtils.addAll(path, field);
			if(visitor.visit(field, describe, subPath) == WalkReturnCode.terminated) return WalkReturnCode.terminated;
		}
		return WalkReturnCode.completed;
	}
	
	public static WalkReturnCode shallowWalkFields(DescribeSObjectResult describe, Visitor visitor) {
		return shallowWalkFields(describe, new Object[]{ describe }, visitor);
	}
	
	public static WalkReturnCode deepWalkFields(Map<String, DescribeSObjectResult> describes, Visitor visitor) {
		for(DescribeSObjectResult describe : describes.values())
			if(deepWalkFields(describe, visitor, describes) == WalkReturnCode.terminated) return WalkReturnCode.terminated;
		return WalkReturnCode.completed;
	}
	
	public static WalkReturnCode deepWalkFields(DescribeSObjectResult describe, Set<String> visited, Object[] path, Visitor visitor, Map<String, DescribeSObjectResult> describes) {
		if(visited.contains(describe.getName()))
			return WalkReturnCode.completed;
		visited.add(describe.getName());
		if(path.length == 0)
			path = new Object[]{ describe };
		for(Field field : describe.getFields()) {
			Object[] subPath = ArrayUtils.addAll(path, field);
			if(visitor.visit(field, describe, subPath) == WalkReturnCode.terminated) return WalkReturnCode.terminated;
			if(field.getType() == FieldType.reference)
				for(String referenceTo : field.getReferenceTo())
					if(deepWalkFields(describes.get(referenceTo), visited, subPath, visitor, describes) == WalkReturnCode.terminated) return WalkReturnCode.terminated;
		}
		return WalkReturnCode.completed;
	}
	
	public static WalkReturnCode deepWalkFields(DescribeSObjectResult describe, Visitor visitor, Map<String, DescribeSObjectResult> describes) {
//		Set<String> visited = new HashSet();
//		visited.add(describe.getName());
		return deepWalkFields(describe, new HashSet(), new Object[0], visitor, describes);
	}

}
