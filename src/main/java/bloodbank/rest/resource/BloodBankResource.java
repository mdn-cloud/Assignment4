/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman update by : I. Am. A. Student 040nnnnnnn
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.BLOODBANK_RESOURCE_NAME;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;

@Path( BLOODBANK_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class BloodBankResource {

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
	public Response getBloodBanks() {
		List< BloodBank> bloodBanks = service.getAll(BloodBank.ALL_BLOODBANKS_QUERY_NAME, BloodBank.class);
		Response response = Response.ok( bloodBanks).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getBloodBankById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		BloodBank bloodBank = null;
		
		bloodBank = service.getById( id, BloodBank.GET_BLOODBANK_BY_ID_QUERY_NAME, BloodBank.class);
		response = Response.status( bloodBank == null ? Status.NOT_FOUND : Status.OK).entity( bloodBank).build();

		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addBloodBank( BloodBank newBloodBank) {
		Response response = null;
		if(service.isDuplicated(BloodBank.IS_DUPLICATE_QUERY_NAME, newBloodBank.getName())) {
			HttpErrorResponse errorResponse = new HttpErrorResponse(Status.CONFLICT.getStatusCode(), "Blood Bank Already Exists");
			response = Response.status(Status.CONFLICT).entity(errorResponse).build();
		} else {
			BloodBank newBloodBankWithIdTimestamps = service.persistEntity( newBloodBank);
			response = newBloodBankWithIdTimestamps != null ? Response.ok( newBloodBankWithIdTimestamps).build()
					: Response.status(Status.BAD_REQUEST).entity("Blood Bank data missing or has wrong format.").build();
		}
		return response;
	}
	
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	@Path("/{id}/bloodDonation")
	public Response addBloodBank(@PathParam( RESOURCE_PATH_ID_ELEMENT) int bloodBankId, BloodDonation newBloodDonation) {
		Response response = null;
		BloodBank bloodBank = service.getById( bloodBankId, BloodBank.GET_BLOODBANK_BY_ID_QUERY_NAME, BloodBank.class);
		if(bloodBank != null) {
			newBloodDonation.setBank(bloodBank);
			bloodBank.getDonations().add(newBloodDonation);
			service.updateEntity(bloodBankId, bloodBank, BloodBank.GET_BLOODBANK_BY_ID_QUERY_NAME, BloodBank.class);
			response = Response.ok(bloodBank).build();
		} else {
			response = Response.status(Status.NOT_FOUND).entity("Blood bank does not exist. Could not add blood donation.").build();
		}
		
		return response;
	}

	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteBloodBank( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deleteEntity(id, BloodBank.GET_BLOODBANK_BY_ID_QUERY_NAME, BloodBank.class);
		response = wasDeleted ? Response.ok("Blood Bank deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Blood bank does not exist. Could not delete.").build();
		return response;
	}
}