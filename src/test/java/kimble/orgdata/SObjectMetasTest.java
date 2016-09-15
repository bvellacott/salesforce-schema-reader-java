package kimble.orgdata;

import static kimble.orgdata.SObjectMetas.*;
import static kimble.orgdata.Mocks.*;
import static org.junit.Assert.*;

import java.util.Map;

import kimble.orgdata.SObjectMetas.SObjectWalkerConfig;
import kimble.orgdata.SObjectMetas.WalkReturnCode;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

public class SObjectMetasTest {
	
	static SObjectWalkerConfig config;
	
	@BeforeClass
	public static void setup() {
		config = new SObjectWalkerConfig();
		config.connection = 
			PartnerConnection()
			.withGlobalDescribe(
				DescribeGlobalResult()
				.withGlobalSObjectDescribes(
					DescribeGlobalSObjectResult().withName("BusinessUnit").build(),
					DescribeGlobalSObjectResult().withName("Account").build(),
					DescribeGlobalSObjectResult().withName("Contact").build())
				.build())
			.withSObjectDescribes(
				(Map)Map()
					.with("BusinessUnit", DescribeSObjectResult()
						.withName("BusinessUnit")
						.withFields(Field().withName("name").withType(FieldType.string).build())
					.build())
					.with("Account", DescribeSObjectResult()
						.withName("Account")
						.withFields(
							Field().withName("name").withType(FieldType.string).build(),
							Field().withName("bu").withType(FieldType.reference).withReferenceTo("BusinessUnit").build())
					.build())
					.with("Contact", DescribeSObjectResult()
						.withName("Contact")
						.withFields(
							Field().withName("name").withType(FieldType.string).build(),
							Field().withName("account").withType(FieldType.reference).withReferenceTo("Account").build())
					.build())
				.build())
			.build();
	}
	
	@Test
	public void getDescribeResultsTest() {
		getDescribeResults(config, metaMap -> {
			assertEquals("result size", 3, metaMap.size());
			DescribeSObjectResult bu = metaMap.get("BusinessUnit");
			DescribeSObjectResult acc = metaMap.get("Account");
			DescribeSObjectResult contact = metaMap.get("Contact");
			assertNotNull("contains BusinessUnit", bu);
			assertNotNull("contains Account", acc);
			assertNotNull("contains Contact", contact);
		}, 5);
	}
	

	int
	totalVisitCount,
	businessUnitNameVisitCount,
	accountNameVisitCount,
	accountBuReferenceFieldVisitCount,
	contactNameVisitCount,
	contactAccountReferenceFieldVisitCount;
	
	@Test
	public void shallowWalkFieldsTest() {
		getDescribeResults(config, metaMap -> {
			totalVisitCount = 0;
			businessUnitNameVisitCount = 0;
			accountNameVisitCount = 0;
			accountBuReferenceFieldVisitCount = 0;
			contactNameVisitCount = 0;
			contactAccountReferenceFieldVisitCount = 0;
			
			shallowWalkFields(metaMap, new Visitor() {

				public WalkReturnCode visit(Field field, DescribeSObjectResult object, Object[] path) {
					totalVisitCount++;
					if(field.getName() == "name") {
						if(object.getName() == "BusinessUnit")
							businessUnitNameVisitCount++;
						if(object.getName() == "Account")
							accountNameVisitCount++;
						if(object.getName() == "Contact")
							contactNameVisitCount++;
					}
					if(field.getName() == "bu" && object.getName() == "Account")
						accountBuReferenceFieldVisitCount++;
					if(field.getName() == "account" && object.getName() == "Contact")
						contactAccountReferenceFieldVisitCount++;
					return WalkReturnCode.completed;
				}
			});
			
			assertEquals("total visits", 5, totalVisitCount);
			assertEquals("business unit name visits", 1, businessUnitNameVisitCount);
			assertEquals("account name visits", 1, accountNameVisitCount);
			assertEquals("account business unit reference visits", 1, accountBuReferenceFieldVisitCount);
			assertEquals("contact name visits", 1, contactNameVisitCount);
			assertEquals("contact account reference visits", 1, contactAccountReferenceFieldVisitCount);
		}, 5);
	}

	@Test
	public void deepWalkFieldsTest() {
		getDescribeResults(config, metaMap -> {
			totalVisitCount = 0;
			businessUnitNameVisitCount = 0;
			accountNameVisitCount = 0;
			accountBuReferenceFieldVisitCount = 0;
			contactNameVisitCount = 0;
			contactAccountReferenceFieldVisitCount = 0;
			
			deepWalkFields(metaMap, new Visitor() {

				public WalkReturnCode visit(Field field, DescribeSObjectResult object, Object[] path) {
					totalVisitCount++;
					if(field.getName() == "name") {
						if(object.getName() == "BusinessUnit")
							businessUnitNameVisitCount++;
						if(object.getName() == "Account")
							accountNameVisitCount++;
						if(object.getName() == "Contact")
							contactNameVisitCount++;
					}
					if(field.getName() == "bu" && object.getName() == "Account")
						accountBuReferenceFieldVisitCount++;
					if(field.getName() == "account" && object.getName() == "Contact")
						contactAccountReferenceFieldVisitCount++;
					return WalkReturnCode.completed;
				}
			});
			
			assertEquals("total visits", 9, totalVisitCount);
			assertEquals("business unit name visits", 3, businessUnitNameVisitCount);
			assertEquals("account name visits", 2, accountNameVisitCount);
			assertEquals("account business unit reference visits", 2, accountBuReferenceFieldVisitCount);
			assertEquals("contact name visits", 1, contactNameVisitCount);
			assertEquals("contact account reference visits", 1, contactAccountReferenceFieldVisitCount);
		}, 5);
	}

	@Test
	public void deepWalkFieldsTest_startingFromASingleObject() {
		getDescribeResults(config, metaMap -> {
			totalVisitCount = 0;
			businessUnitNameVisitCount = 0;
			accountNameVisitCount = 0;
			accountBuReferenceFieldVisitCount = 0;
			contactNameVisitCount = 0;
			contactAccountReferenceFieldVisitCount = 0;
			
			deepWalkFields(metaMap.get("Contact"), new Visitor() {

				public WalkReturnCode visit(Field field, DescribeSObjectResult object, Object[] path) {
					totalVisitCount++;
					if(field.getName() == "name") {
						if(object.getName() == "BusinessUnit")
							businessUnitNameVisitCount++;
						if(object.getName() == "Account")
							accountNameVisitCount++;
						if(object.getName() == "Contact")
							contactNameVisitCount++;
					}
					if(field.getName() == "bu" && object.getName() == "Account")
						accountBuReferenceFieldVisitCount++;
					if(field.getName() == "account" && object.getName() == "Contact")
						contactAccountReferenceFieldVisitCount++;
					return WalkReturnCode.completed;
				}
			}, metaMap);
			
			assertEquals("total visits", 5, totalVisitCount);
			assertEquals("business unit name visits", 1, businessUnitNameVisitCount);
			assertEquals("account name visits", 1, accountNameVisitCount);
			assertEquals("account business unit reference visits", 1, accountBuReferenceFieldVisitCount);
			assertEquals("contact name visits", 1, contactNameVisitCount);
			assertEquals("contact account reference visits", 1, contactAccountReferenceFieldVisitCount);
		}, 5);
	}

}
