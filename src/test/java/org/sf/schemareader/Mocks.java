package org.sf.schemareader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class Mocks {
	
	public static PartnerConnectionBuilder PartnerConnection() { return new PartnerConnectionBuilder(); }
	public static DescribeGlobalResultBuilder DescribeGlobalResult() { return new DescribeGlobalResultBuilder(); }
	public static DescribeGlobalSObjectResultBuilder DescribeGlobalSObjectResult() { return new DescribeGlobalSObjectResultBuilder(); }
	public static DescribeSObjectResultBuilder DescribeSObjectResult() { return new DescribeSObjectResultBuilder(); }
	public static FieldBuilder Field() { return new FieldBuilder(); }
    public static <K,V> MapBuilder<K,V> Map(){ return new MapBuilder<K,V>(new HashMap<K,V>()); }

	public static class PartnerConnectionBuilder {
		
		DescribeGlobalResult mockGlobalDescribe;
		Map<String, DescribeSObjectResult> mockSObjectDescribes = Collections.emptyMap();
		
		PartnerConnection o;
		{try { 
			o = new PartnerConnection(new ConnectorConfig() {
				public void verifyPartnerEndpoint() {}
				public boolean isManualLogin() { return true; }
				public String getServiceEndpoint() { return "https://endpoint.com"; }
				public String getSessionId() { return "session123"; }
			}) {
				public DescribeGlobalResult describeGlobal() { return mockGlobalDescribe; }
				public DescribeSObjectResult[] describeSObjects(String[] sObjectNames) {
					DescribeSObjectResult[] results = new DescribeSObjectResult[sObjectNames.length];
					for(int i = 0; i < sObjectNames.length; i++)
						results[i] = mockSObjectDescribes.get(sObjectNames[i]);
					return results;
				}
			};
		} catch (ConnectionException e) { throw new RuntimeException("failed to create mock partner connection", e); }}
		
		public PartnerConnectionBuilder withGlobalDescribe(DescribeGlobalResult aGlobalDescribe) { mockGlobalDescribe = aGlobalDescribe; return this; }
		public PartnerConnectionBuilder withSObjectDescribes(Map<String, DescribeSObjectResult> aSObjectDescribes) { mockSObjectDescribes = aSObjectDescribes; return this; }
		
		public PartnerConnection build() { return o; }
	}
	
	public static class DescribeGlobalResultBuilder {
		
		DescribeGlobalSObjectResult[] mockGlobalSObjectDescribes = new DescribeGlobalSObjectResult[0];
		
		DescribeGlobalResult o;
		{
			o = new DescribeGlobalResult() {
				public DescribeGlobalSObjectResult[] getSobjects() { return mockGlobalSObjectDescribes; }
			};
		}
		
		public DescribeGlobalResultBuilder withGlobalSObjectDescribes(DescribeGlobalSObjectResult... aGlobalSObjectDescribes) { mockGlobalSObjectDescribes = aGlobalSObjectDescribes; return this; }
		
		public DescribeGlobalResult build() { return o; }
	}
	
	public static class DescribeGlobalSObjectResultBuilder {
		
		String name;
		
		DescribeGlobalSObjectResult o;
		{
			o = new DescribeGlobalSObjectResult() {
				public String getName() { return name; }
			};
		}
		
		public DescribeGlobalSObjectResultBuilder withName(String aName) { name = aName; return this;}
		
		public DescribeGlobalSObjectResult build() { return o; }
	}
	
	public static class DescribeSObjectResultBuilder {
		
		String name;
		Field[] fields = new Field[0];
		
		DescribeSObjectResult o;
		{
			o = new DescribeSObjectResult() {
				public String getName() { return name; }
				public Field[] getFields() { return fields; }
			};
		}
		
		public DescribeSObjectResultBuilder withName(String aName) { name = aName; return this; }
		public DescribeSObjectResultBuilder withFields(Field... aFields) { fields = aFields; return this; }
		
		public DescribeSObjectResult build() { return o; }
		
	}
	
	public static class FieldBuilder {
		
		String name;
		FieldType type;
		String[] referenceTo = new String[0];
		
		Field o;
		{
			o = new Field() {
				public String getName() { return name; }
				public FieldType getType() { return type; }
				public String[] getReferenceTo() { return referenceTo; }
			};
		}
		
		public FieldBuilder withName(String aName) { name = aName; return this; }
		public FieldBuilder withType(FieldType aType) { type = aType; return this; }
		public FieldBuilder withReferenceTo(String... aReferenceTo) { referenceTo = aReferenceTo; return this; }

		public Field build() { return o; }
		
	}

	public static class MapBuilder<K,V> {

	    private Map<K,V> map;

	    public MapBuilder(Map<K,V> map) {
	        this.map = map;
	    }

	    public MapBuilder<K,V> with(K key, V value){
	        map.put(key, value);
	        return this;
	    }

	    public Map<K,V> build(){
	        return map;
	    }

	}
}
