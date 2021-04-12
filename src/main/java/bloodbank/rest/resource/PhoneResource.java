/**
 * File: PhoneResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman update by : I. Am. A. Student 040nnnnnnn
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.PHONE_RESOURCE_NAME;
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
import bloodbank.entity.Phone;

@Path( PHONE_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class PhoneResource {

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
	public Response getPhones() {
		List< Phone> phones = service.getAll(Phone.ALL_PHONES_QUERY_NAME, Phone.class);
		Response response = Response.ok( phones).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getPhoneById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		Phone phone = null;
		
		phone = service.getById( id, Phone.GET_PHONE_BY_ID_QUERY_NAME, Phone.class);
		response = Response.status( phone == null ? Status.NOT_FOUND : Status.OK).entity( phone).build();

		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addPhone( Phone newPhone) {
		Response response = null;
		if(service.isPhoneDuplicated(newPhone)) {
			HttpErrorResponse errorResponse = new HttpErrorResponse(Status.CONFLICT.getStatusCode(), "Phone Already Exists");
			response = Response.status(Status.CONFLICT).entity(errorResponse).build();
		} else {
			Phone newPhoneWithIdTimestamps = service.persistEntity( newPhone);
			response = newPhoneWithIdTimestamps != null ? Response.ok( newPhoneWithIdTimestamps).build()
					: Response.status(Status.BAD_REQUEST).entity("Phone data missing or has wrong format.").build();
		}
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deletePhone( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deletePhone(id);
		response = wasDeleted ? Response.ok("Phone deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Phone does not exist. Could not delete.").build();
		return response;
	}
}