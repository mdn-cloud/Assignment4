/**
 * File: RestConfig.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @date Mar 31, 2021
 * @author Mike Norman
 * @date 2020 10
 */
package bloodbank.rest.serializer;

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Person;

public class PersonSerializer extends StdSerializer< Person> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private static final Logger LOG = LogManager.getLogger();
	
    @EJB
    protected BloodBankService service;

	public PersonSerializer() {
		this(null);
	}

	public PersonSerializer( Class< Person> t) {
		super(t);
	}

	@Override
	public void serialize( Person original, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		LOG.trace("serializeing={}",original);
		generator.writeStartObject();
		generator.writeNumberField( "id", original.getId());
		generator.writeStringField("first_name", original.getFirstName());
		generator.writeStringField("last_name", original.getLastName());

		int count = original.getDonations() == null ? 0 : original.getDonations().size();
		generator.writeNumberField( "donation_count", count);

		generator.writeObjectField( "created", original.getCreated());
		generator.writeObjectField( "updated", original.getUpdated());
		generator.writeNumberField( "version", original.getVersion());
		generator.writeEndObject();
	}
}