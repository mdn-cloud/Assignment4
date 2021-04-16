/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman 
 * 
 * Update by : Yves Ferland  
 * @author Ra'ad Sweidan
 * @author Mukta Debnath
 * @author Thanh Nguyen
 * @author Yves Ferland 
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.BLOOD_DONATION_RESOURCE_NAME;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodDonation;

@Path( BLOOD_DONATION_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class BloodDonationResource {

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
	public Response getBloodDonations() {
		List< BloodDonation> bloodDonations = service.getAll("BloodDonation.findAll", BloodDonation.class);
		Response response = Response.ok( bloodDonations).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getBloodDonationById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		BloodDonation bloodDonation = null;
		
		bloodDonation = service.getById( id, BloodDonation.GET_BLOOD_DONATION_BY_ID_QUERY_NAME, BloodDonation.class);
		response = Response.status( bloodDonation == null ? Status.NOT_FOUND : Status.OK).entity( bloodDonation).build();

		return response;
	}

	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteBloodDonation( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deleteEntity(id, BloodDonation.GET_BLOOD_DONATION_BY_ID_QUERY_NAME, BloodDonation.class);
		response = wasDeleted ? Response.ok("Blood donation deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Blood donation does not exist. Could not delete.").build();
		return response;
	}
}