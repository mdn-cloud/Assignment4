/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman 
 * 
 * Update by : Ra'ad Sweidan 
 * @author Ra'ad Sweidan
 * @author Mukta Debnath
 * @author Thanh Nguyen
 * @author Yves Ferland
 * 
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.DONATION_RECORD_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.glassfish.soteria.WrappingCallerPrincipal;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;

@Path( DONATION_RECORD_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class DonationRecordResource {

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE})
	public Response getDonationRecords() {
		List< DonationRecord> donationRecords = service.getAll(DonationRecord.ALL_RECORDS_QUERY_NAME, DonationRecord.class);
		Response response = Response.ok( donationRecords).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getDonationRecordById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		DonationRecord donationRecord = service.getById( id, DonationRecord.GET_DONATION_RECORD_BY_ID_QUERY_NAME, DonationRecord.class);
		
		if(donationRecord == null) {
			response = Response.status(Status.NOT_FOUND).entity( donationRecord).build();
	
		} else if ( sc.isCallerInRole( ADMIN_ROLE)) {
			response = Response.status(Status.OK).entity( donationRecord).build();
			
		} else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			Person person = sUser.getPerson();
			
			if ( person != null && person.equals(donationRecord.getOwner())) {
				response = Response.status( Status.OK).entity( donationRecord).build();
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		} else {
			response = Response.status( Status.BAD_REQUEST).build();
		}
		return response;
	}

	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteBloodDonation( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deleteEntity(id, DonationRecord.GET_DONATION_RECORD_BY_ID_QUERY_NAME, DonationRecord.class);
		response = wasDeleted ? Response.ok("Donation record deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Donation record does not exist. Could not delete.").build();
		return response;
	}
}