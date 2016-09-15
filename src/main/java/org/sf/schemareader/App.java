package org.sf.schemareader;

import static org.sf.schemareader.SObjectMetas.deepWalkFields;
import static org.sf.schemareader.SObjectMetas.getDescribeResults;

import org.sf.schemareader.SObjectMetas.SObjectWalkerConfig;
import org.sf.schemareader.SObjectMetas.Visitor;
import org.sf.schemareader.SObjectMetas.WalkReturnCode;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class App 
{
    public static void main( String[] args )
    {
        String username = "username";
        String password = "password";
        String authEndPoint = "https://login.salesforce.com/services/Soap/u/22.0";
        SObjectWalkerConfig walkerConfig = new SObjectWalkerConfig();

        try {
           ConnectorConfig config = new ConnectorConfig();
           config.setUsername(username);
           config.setPassword(password);

           System.out.println("AuthEndPoint: " + authEndPoint);
           config.setAuthEndpoint(authEndPoint);

           walkerConfig.connection = new PartnerConnection(config);
        } catch (ConnectionException e) { throw new RuntimeException("failed to connect", e); }

		getDescribeResults(walkerConfig, metaMap -> {
			deepWalkFields(metaMap, new Visitor() {
	
				public WalkReturnCode visit(Field field, DescribeSObjectResult object, Object[] path) {
					if(path.length > 2)
						System.out.println(object.getName() + "." + field.getName() + " : path length = " + path.length);
					return WalkReturnCode.completed;
				}
			});
		}, 30);
    }
}
