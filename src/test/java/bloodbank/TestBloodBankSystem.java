/**
 * File: OrderSystemTestSuite.java
 * Course materials (20F) CST 8277
 * (Original Author) Mike Norman
 *
 * @date 2020 10
 *
 * (Modified)
 * @author Ra'ad Sweidan
 * @author Mukta Debnath
 * @author Thanh Nguyen
 * @author Yves Ferland 
 */
package bloodbank;

import bloodbank.utility.MyConstants;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.entity.Address;
import bloodbank.entity.BloodBank;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestBloodBankSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank-Skeleton";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    
    // References to Id values of newly created entities used to keep track across tests
    static int newPersonId, newBloodBankId, newPhoneId, newAddressId;

    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    static HttpAuthenticationFeature cstAuth;
    
    // Helper method for calling the get all end points (with an admin) and checking the returned results size
    public <T> void getAllAndCheckSize(String resourceName, int size) {
    	Response response = webTarget
                .register(adminAuth)
                .path(resourceName)
                .request()
                .get();
            assertThat(response.getStatus(), is(200));
            List<T> resultsList = response.readEntity(new GenericType<List<T>>(){});
            assertThat(resultsList, size == 0 ? is(empty()) : is(not(empty())));
            assertThat(resultsList, hasSize(size));
    }
    
    // Helper method that uses the non admin user to call a get all end point and checks if a specific status code is returned
    public void getAllAndCheckStatus(String resourceName, int statusCode) {
        Response response = webTarget
            .register(cstAuth)
            .path(resourceName)
            .request()
            .get();
        assertThat(response.getStatus(), is(statusCode));
    }

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + MyConstants.APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(MyConstants.DEFAULT_ADMIN_USER, MyConstants.DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(MyConstants.DEFAULT_USER_PREFIX, MyConstants.DEFAULT_USER_PASSWORD);
        cstAuth = HttpAuthenticationFeature.basic(MyConstants.CST_USER, MyConstants.CST_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }
    
    @Test
    @Order(1)
    public void getAllPersonsAdmin() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.PERSON_RESOURCE_NAME, 1);
    }
    
    @Test
    @Order(2)
    public void getAllPersonsUser() throws JsonMappingException, JsonProcessingException {
    	getAllAndCheckStatus(MyConstants.PERSON_RESOURCE_NAME, 403); // Only admins can get all persons
    }
    
    @Test
    @Order(3)
    public void getPersonByIdAdmin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Person person = response.readEntity(Person.class);
        assertNotEquals(null, person);
        assertEquals(1, person.getId());
    }
    
    @Test
    @Order(4)
    // Test that a person can read their own record in the database
    public void getPersonByIdSameUser() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(cstAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Person person = response.readEntity(Person.class);
        assertNotEquals(null, person);
        assertEquals(1, person.getId());
    }
    
    @Test
    @Order(5)
    public void createUser() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("firstName", "Ra'ad");
    	jsonBody.put("lastName", "Sweidan");
    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME)
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        Person person = response.readEntity(Person.class);
        newPersonId = person.getId();
        assertNotEquals(null, person);
        
        // Now check that the person count has increased
        getAllAndCheckSize(MyConstants.PERSON_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(6)
    // Try and read a person with a user that's not the person being read. This request should be blocked
    public void getPersonByIdDifferentUser() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(cstAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME + "/" + newPersonId)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }
    
    @Test
    @Order(7)
    public void getAllDonationRecords() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.DONATION_RECORD_RESOURCE_NAME, 0);
    }
    
    @Test
    @Order(8)
    public void getAllDonationRecordsUser() throws JsonMappingException, JsonProcessingException {
    	// Normal users can't get all donation records
    	getAllAndCheckStatus(MyConstants.DONATION_RECORD_RESOURCE_NAME, 403);
    }
    
    @Test
    @Order(9)
    public void createDonationRecordForPerson() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("tested", false);
    	    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME + "/" + newPersonId + "/donationRecord")
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        Person bb = response.readEntity(Person.class); // Creating a donation record returns the associated person
        assertNotEquals(null, bb);
        assertEquals(newPersonId, bb.getId());
        
        // Check that a new blood donation was added
        getAllAndCheckSize(MyConstants.DONATION_RECORD_RESOURCE_NAME, 1);
    }
    
    @Test
    @Order(10)
    public void deletePerson() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PERSON_RESOURCE_NAME + "/" + newPersonId)
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        
        // Now check that the person count has decreased
        getAllAndCheckSize(MyConstants.PERSON_RESOURCE_NAME, 1);
        // Check that the blood donation was deleted along with the owning person
        getAllAndCheckSize(MyConstants.DONATION_RECORD_RESOURCE_NAME, 0);
    }
    
    @Test
    @Order(11)
    public void getAllBloodBanksAdmin() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.BLOODBANK_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(12)
    public void getAllBloodBanksUser() throws JsonMappingException, JsonProcessingException {
    	// Any user can view all blood banks
    	getAllAndCheckStatus(MyConstants.BLOODBANK_RESOURCE_NAME, 200);
    }
    
    @Test
    @Order(13)
    public void getSpecificBloodBankAdmin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.BLOODBANK_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        BloodBank bb = response.readEntity(BloodBank.class);
        assertNotEquals(null, bb);
        assertEquals(1, bb.getId());
        assertEquals("Bloody Bank", bb.getName());
    }
    
    @Test
    @Order(14)
    // Normal users can also get specific blood banks
    public void getSpecificBloodBankUser() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(cstAuth)
            .path(MyConstants.BLOODBANK_RESOURCE_NAME + "/2")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        BloodBank bb = response.readEntity(BloodBank.class);
        assertNotEquals(null, bb);
        assertEquals(2, bb.getId());
        assertEquals("Cheap Bloody Bank", bb.getName());
    }
    
    @Test
    @Order(15)
    public void createBloodBank() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("name", "New Bank");
    	jsonBody.put("is_public", true);
    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.BLOODBANK_RESOURCE_NAME)
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        BloodBank bb = response.readEntity(BloodBank.class);
        newBloodBankId = bb.getId();
        assertNotEquals(null, bb);
        assertEquals("New Bank", bb.getName());
        // Now check that the blood bank count has increased
        getAllAndCheckSize(MyConstants.BLOODBANK_RESOURCE_NAME, 3);
    }
    
    @Test
    @Order(16)
    public void createBloodBankWithNonAdmin() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("name", "New Bank");
    	jsonBody.put("is_public", true);
    	
        Response response = webTarget
            .register(cstAuth) // Only admins can create blood banks
            .path(MyConstants.BLOODBANK_RESOURCE_NAME)
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(403));
        // Now check that the person count has not changed
        getAllAndCheckSize(MyConstants.BLOODBANK_RESOURCE_NAME, 3);
    }
    
    @Test
    @Order(17)
    public void getAllBloodDonationsAdmin() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.BLOOD_DONATION_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(18)
    public void getAllBloodDonationsUser() throws JsonMappingException, JsonProcessingException {
    	// Any user can view all blood donations
    	getAllAndCheckStatus(MyConstants.BLOOD_DONATION_RESOURCE_NAME, 200);
    }
    
    @Test
    @Order(19)
    public void createBloodDonationForBloodBank() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("milliliters", 11);
    	
    	Map<String, Object> bloodTypeJson = new HashMap<>(); // Inner node containing the blood type
    	bloodTypeJson.put("bloodGroup", "O");
    	bloodTypeJson.put("rhd", "0");
    	
    	jsonBody.put("bloodType", bloodTypeJson);
    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.BLOODBANK_RESOURCE_NAME + "/" + newBloodBankId + "/bloodDonation")
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        BloodBank bb = response.readEntity(BloodBank.class); // Creating a blood donation returns the associated blood bank
        assertNotEquals(null, bb);
        assertEquals(newBloodBankId, bb.getId());
        assertEquals("New Bank", bb.getName());
        
        // Check that a new blood donation was added
        getAllAndCheckSize(MyConstants.BLOOD_DONATION_RESOURCE_NAME, 3);
    }
    
    @Test
    @Order(20)
    public void deleteBloodBank() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.BLOODBANK_RESOURCE_NAME + "/" + newBloodBankId)
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        
        // Now check that the blood bank count has decreased
        getAllAndCheckSize(MyConstants.BLOODBANK_RESOURCE_NAME, 2);
        // The newly created blood donation will also be deleted with its parent blood bank
        getAllAndCheckSize(MyConstants.BLOOD_DONATION_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(21)
    public void getAllPhonesAdmin() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.PHONE_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(22)
    public void getAllPhonesUser() throws JsonMappingException, JsonProcessingException {
    	getAllAndCheckStatus(MyConstants.PHONE_RESOURCE_NAME, 200); // Anyone can view all phones
    }
    
    @Test
    @Order(23)
    public void getPhoneById() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PHONE_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Phone phone = response.readEntity(Phone.class);
        assertNotEquals(null, phone);
        assertEquals(1, phone.getId());
        assertEquals("0", phone.getCountryCode());
        assertEquals("234", phone.getAreaCode());
        assertEquals("5678900", phone.getNumber());
    }
    
    @Test
    @Order(24)
    public void createPhone() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("countryCode", "1");
    	jsonBody.put("areaCode", "123");
    	jsonBody.put("number", "1234567");
    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PHONE_RESOURCE_NAME)
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        Phone phone = response.readEntity(Phone.class);
        newPhoneId = phone.getId();
        assertNotEquals(null, phone);
        assertEquals("1", phone.getCountryCode());
        assertEquals("123", phone.getAreaCode());
        assertEquals("1234567", phone.getNumber());
        
        // Now check that the phone count has increased
        getAllAndCheckSize(MyConstants.PHONE_RESOURCE_NAME, 3);
    }
   
    @Test
    @Order(25)
    public void deletePhone() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.PHONE_RESOURCE_NAME + "/" + newPhoneId)
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        
        // Now check that the phone count has decreased
        getAllAndCheckSize(MyConstants.PHONE_RESOURCE_NAME, 2);
    }
    
    @Test
    @Order(26)
    public void getAllAddressesAdmin() throws JsonMappingException, JsonProcessingException {
        getAllAndCheckSize(MyConstants.ADDRESS_RESOURCE_NAME, 1);
    }
    
    @Test
    @Order(27)
    public void getAllAddressesUser() throws JsonMappingException, JsonProcessingException {
    	getAllAndCheckStatus(MyConstants.ADDRESS_RESOURCE_NAME, 200); // Anyone can view all addresses
    }
    
    @Test
    @Order(28)
    public void getAddressById() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.ADDRESS_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Address address = response.readEntity(Address.class);
        assertNotEquals(null, address);
        assertEquals(1, address.getId());
        assertEquals("123", address.getStreetNumber());
        assertEquals("abcd Dr.W", address.getStreet());
        assertEquals("A1B2C3", address.getZipcode());
    }
    
    @Test
    @Order(29)
    public void createAddress() throws JsonMappingException, JsonProcessingException {
    	Map<String, Object> jsonBody = new HashMap<>();
    	jsonBody.put("streetNumber", "456");
    	jsonBody.put("city", "Toronto");
    	jsonBody.put("country", "CA");
    	jsonBody.put("province", "ON");
    	jsonBody.put("street", "West East North Street");
    	jsonBody.put("zipcode", "K1897");
    	
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.ADDRESS_RESOURCE_NAME)
            .request()
            .post(Entity.json(jsonBody));
        
        assertThat(response.getStatus(), is(200));
        Address address = response.readEntity(Address.class);
        newAddressId = address.getId();
        assertNotEquals(null, address);
        assertEquals("456", address.getStreetNumber());
        assertEquals("Toronto", address.getCity());
        assertEquals("CA", address.getCountry());
        assertEquals("ON", address.getProvince());
        assertEquals("West East North Street", address.getStreet());
        assertEquals("K1897", address.getZipcode());
        
        // Now check that the address count has increased
        getAllAndCheckSize(MyConstants.PHONE_RESOURCE_NAME, 2);
    }
   
    @Test
    @Order(30)
    public void deleteAddress() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MyConstants.ADDRESS_RESOURCE_NAME + "/" + newAddressId)
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        
        // Now check that the address count has decreased
        getAllAndCheckSize(MyConstants.ADDRESS_RESOURCE_NAME, 1);
    }
       
}
