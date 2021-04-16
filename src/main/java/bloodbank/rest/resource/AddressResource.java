/**
 * File: AddressResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman 
 * 
 * update by : Mukta Debnath 
 * @author Ra'ad Sweidan
 * @author Mukta Debnath
 * @author Thanh Nguyen
 * @author Yves Ferland 
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.ADDRESS_RESOURCE_NAME;
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
import bloodbank.entity.Address;

@Path(ADDRESS_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class AddressResource {

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
	public Response getAddresss() {
		List< Address> addresss = service.getAll(Address.ALL_ADRESSES_QUERY_NAME, Address.class);
		Response response = Response.ok( addresss).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getAddressById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		Address address = null;
		
		address = service.getById( id, Address.GET_ADDRESS_BY_ID_QUERY_NAME, Address.class);
		response = Response.status( address == null ? Status.NOT_FOUND : Status.OK).entity( address).build();

		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addAddress( Address newAddress) {
		Response response = null;
		if(service.isAddressDuplicated(newAddress)) {
			HttpErrorResponse errorResponse = new HttpErrorResponse(Status.CONFLICT.getStatusCode(), "Address Already Exists");
			response = Response.status(Status.CONFLICT).entity(errorResponse).build();
		} else {
			Address newAddressWithIdTimestamps = service.persistEntity( newAddress);
			response = newAddressWithIdTimestamps != null ? Response.ok( newAddressWithIdTimestamps).build()
					: Response.status(Status.BAD_REQUEST).entity("Address data missing or has wrong format.").build();
		}
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteAddress( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response = null;
		boolean wasDeleted = service.deleteAddress(id);
		response = wasDeleted ? Response.ok("Address deleted").build() : 
			Response.status(Status.NOT_FOUND).entity("Address does not exist. Could not delete.").build();
		return response;
	}
}