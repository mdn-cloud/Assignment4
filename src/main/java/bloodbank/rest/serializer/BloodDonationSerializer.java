/**
 * File: RestConfig.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @date Mar 31, 2021
 * @author Mike Norman
 * @date 2020 10
 * 
 * Update by : Thanh Nguyen
 * @author Ra'ad Sweidan
 * @author Mukta Debnath
 * @author Thanh Nguyen
 * @author Yves Ferland
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
import bloodbank.entity.BloodDonation;

public class BloodDonationSerializer extends StdSerializer< BloodDonation> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private static final Logger LOG = LogManager.getLogger();
	
    @EJB
    protected BloodBankService service;

	public BloodDonationSerializer() {
		this(null);
	}

	public BloodDonationSerializer( Class< BloodDonation> t) {
		super(t);
	}

	@Override
	public void serialize( BloodDonation original, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		LOG.trace("serializeing={}",original);
		generator.writeStartObject();
		generator.writeNumberField( "id", original.getId());
		int bankId = original.getBank().getId();
		generator.writeNumberField( "bank_id", bankId);
		generator.writeNumberField( "milliliters", original.getMilliliters());
		generator.writeObjectField( "blood_type", original.getBloodType());
		generator.writeObjectField( "created", original.getCreated());
		generator.writeObjectField( "updated", original.getUpdated());
		generator.writeNumberField( "version", original.getVersion());
		generator.writeEndObject();
	}
}