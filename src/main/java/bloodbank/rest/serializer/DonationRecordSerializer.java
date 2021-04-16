/**
 * File: RestConfig.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @date Mar 31, 2021
 * @author Mike Norman
 * @date 2020 10
 * 
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
import bloodbank.entity.DonationRecord;

public class DonationRecordSerializer extends StdSerializer< DonationRecord> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private static final Logger LOG = LogManager.getLogger();
	
    @EJB
    protected BloodBankService service;

	public DonationRecordSerializer() {
		this(null);
	}

	public DonationRecordSerializer( Class< DonationRecord> t) {
		super(t);
	}

	@Override
	public void serialize( DonationRecord original, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		LOG.trace("serializeing={}",original);
		generator.writeStartObject();
		generator.writeNumberField( "id", original.getId());
		int personId = original.getOwner().getId();
		generator.writeNumberField( "owner_id", personId);
		
		if(original.getDonation() != null) { // BloodDonations are optional so need to check
			int donationId = original.getDonation().getId();
			generator.writeNumberField( "donation_id", donationId);
		}
		
		boolean isTested = original.getTested() == 1;
		generator.writeBooleanField("is_tested", isTested);
		generator.writeObjectField( "created", original.getCreated());
		generator.writeObjectField( "updated", original.getUpdated());
		generator.writeNumberField( "version", original.getVersion());
		generator.writeEndObject();
	}
}