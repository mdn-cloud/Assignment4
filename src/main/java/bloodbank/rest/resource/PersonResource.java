/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman update by : I. Am. A. Student 040nnnnnnn
 */
package bloodbank.rest.resource;

import static bloodbank.entity.Person.GET_PERSON_BY_ID_QUERY_NAME;
import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;

@Path( PERSON_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class PersonResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE})
	public Response getPersons() {
		List< Person> persons = service.getAll(Person.ALL_PERSONS_QUERY_NAME, Person.class);
		Response response = Response.ok( persons).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getPersonById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug( "try to retrieve specific person " + id);
		Response response = null;
		Person person = null;

		if ( sc.isCallerInRole( ADMIN_ROLE)) {
			person = service.getById( id, GET_PERSON_BY_ID_QUERY_NAME, Person.class);
			response = Response.status( person == null ? Status.NOT_FOUND : Status.OK).entity( person).build();
		} else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			person = sUser.getPerson();
			if ( person != null && person.getId() == id) {
				response = Response.status( Status.OK).entity( person).build();
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		} else {
			response = Response.status( Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addPerson( Person newPerson) {
		Response response = null;
		// build a SecurityUser linked to the new person
		Person persistedPerson = service.buildUserForNewPerson( newPerson);		
		response = persistedPerson != null ? Response.ok( persistedPerson).build()
				: Response.status(Status.BAD_REQUEST).entity("Person data missing or has wrong format.").build();
		return response;
	}
	
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	@Path("/{id}/donationRecord")
	public Response addDonationRecord(@PathParam( RESOURCE_PATH_ID_ELEMENT) int personId, DonationRecord newDonationRecord) {
		Response response = null;
		Person person = service.getById(personId, GET_PERSON_BY_ID_QUERY_NAME, Person.class);
		if(person != null) {
			newDonationRecord.setOwner(person);
			person.getDonations().add(newDonationRecord);
			service.updateEntity(personId, person, Person.GET_PERSON_BY_ID_QUERY_NAME, Person.class);
			response = Response.ok(person).build();
		} else {
			response = Response.status(Status.NOT_FOUND).entity("Person does not exist. Could not add donation record.").build();
		}
		
		return response;
	}
	
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	@Path("/{personId}/bloodDonation/{bloodDonationId}/donationRecord")
	public Response addDonationRecordWithBloodDonation(@PathParam("personId") int personId, @PathParam("bloodDonationId") int bloodDonationId,
			DonationRecord newDonationRecord) {
		Response response = null;
		Person person = service.getById(personId, GET_PERSON_BY_ID_QUERY_NAME, Person.class);
		BloodDonation bloodDonation = service.getById(bloodDonationId, BloodDonation.GET_BLOOD_DONATION_BY_ID_QUERY_NAME, BloodDonation.class);
		
		if(person == null) {
			response = Response.status(Status.NOT_FOUND).entity("Person does not exist. Could not add donation record.").build();
			
		} else if(bloodDonation == null) {
			response = Response.status(Status.NOT_FOUND).entity("Blood Donation does not exist. Could not add donation record.").build();
			
		} else if(bloodDonation.getRecord() != null) {
			response = Response.status(Status.CONFLICT).entity("Blood Donation already has a donation record. Could not add donation record.").build();
		} else {
			newDonationRecord.setOwner(person);
			person.getDonations().add(newDonationRecord);
			newDonationRecord.setDonation(bloodDonation);
			
			service.updateEntity(personId, person, Person.GET_PERSON_BY_ID_QUERY_NAME, Person.class);
			response = Response.ok(person).build();
		}
		
		return response;
	}

	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deletePerson( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deletePersonById(id);
		response = wasDeleted ? Response.ok("Person deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Person does not exist. Could not delete.").build();
		return response;
	}
}