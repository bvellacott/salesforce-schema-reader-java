# salesforce-schema-reader-java
Before using this tool on orgs make sure the orgs comply with the prerequisites stated at:
https://developer.salesforce.com/docs/atlas.en-us.api_streaming.meta/api_streaming/code_sample_java_prereqs.htm

This tool will help you in reading a salesforce database schema. You can read the entire schema using a visitor. A visitor is a function which takes context information as parameters. Specifically a visitor in this tool will be given the current field being visited, the object that the field belongs to and the path that was followed to get to that field.

The reader takes care of not traversing circular dpendencies i.e. if there is an object called Human with a reference to an object called Pet with a reference back to Human, that would be a circular dependency and if not handled will cause an infinite loop. The reader handles these scenarios simply by keeping track of all the traversed objects and making sure no object gets traversed twice.

### JS equivalent
This project is the java equivalent of the js project salesforce-schema-reader, at https://github.com/bvellacott/salesforce-schema-reader except this project doesn't support child relationship traversal simply because I haven't written the code for it yet. If you want to, you can. It is pretty straight forward. Have a look at the javascript equivalent for a deeper understanding of what the tool does. It has better documentation.

### JitPak
I recommend using JitPack at https://jitpack.io/ if you would like be able to reference this project as a maven/gradle dependency. A whole new world for java that! 

##Example:
```
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
```

## Thanks for reading!
And let me know if you have issues and you want to fix them





