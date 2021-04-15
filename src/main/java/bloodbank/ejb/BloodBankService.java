/**
 * File: RecordService.java
 * Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * update by : I. Am. A. Student 040nnnnnnn
 *
 */
package bloodbank.ejb;

import static bloodbank.entity.Person.GET_PERSON_BY_ID_QUERY_NAME;
import static bloodbank.entity.SecurityRole.ROLE_BY_NAME_QUERY;
import static bloodbank.entity.SecurityUser.USER_FOR_OWNING_PERSON_QUERY;
import static bloodbank.utility.MyConstants.DEFAULT_KEY_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.DEFAULT_SALT_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PARAM1;
import static bloodbank.utility.MyConstants.PARAM2;
import static bloodbank.utility.MyConstants.PARAM3;
import static bloodbank.utility.MyConstants.PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.PROPERTY_KEYSIZE;
import static bloodbank.utility.MyConstants.PROPERTY_SALTSIZE;
import static bloodbank.utility.MyConstants.PU_NAME;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import bloodbank.entity.Address;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import bloodbank.entity.SecurityRole;
import bloodbank.entity.SecurityUser;


/**
 * Stateless Singleton ejb Bean - BloodBankService
 */
@Singleton
public class BloodBankService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;
    
    public <T> List<T> getAll(String queryName, Class<T> entityType) {
    	TypedQuery<T> findAll = em.createNamedQuery(queryName, entityType);
        return findAll.getResultList();
    }
    
    public <T> T getById(int id, String queryName, Class<T> entityType) {
    	T entity;
    	try {
    		TypedQuery<T> findEntity = em
                    .createNamedQuery(queryName, entityType)
                    .setParameter(PARAM1, id);
       	 entity = findEntity.getSingleResult();
    	} catch (NoResultException ex) {
    		entity = null;
    	}
    	return entity; 
    }
    
    public boolean isDuplicated(String queryName, String searchParam) {
    	TypedQuery<Long> duplicateQuery = em.createNamedQuery(queryName, Long.class);
    	duplicateQuery.setParameter(PARAM1, searchParam);	
    	return duplicateQuery.getSingleResult() != 0;			
    }
    
    public boolean isAddressDuplicated(Address address) {
    	TypedQuery<Long> duplicateQuery = em.createNamedQuery(Address.IS_DUPLICATE_QUERY_NAME, Long.class);
    	duplicateQuery.setParameter(PARAM1, address.getZipcode());	
    	duplicateQuery.setParameter(PARAM2, address.getStreet());	
    	duplicateQuery.setParameter(PARAM3, address.getStreetNumber());	
    	return duplicateQuery.getSingleResult() != 0;			
    }
    
    public boolean isPhoneDuplicated(Phone phone) {
    	TypedQuery<Long> duplicateQuery = em.createNamedQuery(Phone.IS_DUPLICATE_QUERY_NAME, Long.class);
    	duplicateQuery.setParameter(PARAM1, phone.getCountryCode());	
    	duplicateQuery.setParameter(PARAM2, phone.getAreaCode());	
    	duplicateQuery.setParameter(PARAM3, phone.getNumber());	
    	return duplicateQuery.getSingleResult() != 0;
    }
    
    @Transactional
    public <T> T persistEntity(T entity) {
    	try {
    		em.persist(entity);
    	} catch (Exception ex) {
    		return null;
    	}
    	return entity;
    }
    
    @Transactional
    public <T> T updateEntity(int id, T entityWithUpdates, String queryName, Class<T> entityType ) {
        T entityToBeUpdated = getById(id, queryName, entityType);
        if (entityToBeUpdated != null) {
            em.refresh(entityToBeUpdated);
            em.merge(entityWithUpdates);
            em.flush();
        }
        return entityToBeUpdated;
    }
    
    // Generic delete by entity method. Only for entities that dont need extra delete logic
    @Transactional
    public <T> boolean deleteEntity(int id, String getByIdQueryName, Class<T> entityType) {
        T entity = getById(id, getByIdQueryName, entityType);
        if (entity != null) {
            em.remove(entity);
            return true;
        } else {
        	return false;
        }
    }
    
    @Transactional
    public boolean deletePhone(int id) {
        Phone phoneToDelete = getById(id, Phone.GET_PHONE_BY_ID_QUERY_NAME, Phone.class);
        if (phoneToDelete != null) {
        	phoneToDelete.getContacts().forEach((contact) -> em.remove(contact));
            em.remove(phoneToDelete);
            return true;
        } else {
        	return false;
        }
    }
    
    @Transactional
    public boolean deleteAddress(int id) {
        Address addressToDelete = getById(id, Address.GET_ADDRESS_BY_ID_QUERY_NAME, Address.class);
        if (addressToDelete != null) {
        	addressToDelete.getContacts().forEach((contact) -> em.remove(contact));
            em.remove(addressToDelete);
            return true;
        } else {
        	return false;
        }
    }
    
    @Transactional
    public Person buildUserForNewPerson(Person newPerson) {
    	try {
    		 SecurityUser userForNewPerson = new SecurityUser();
    	        userForNewPerson.setUsername(
    	            DEFAULT_USER_PREFIX + "_" + newPerson.getFirstName() + "." + newPerson.getLastName());
    	        Map<String, String> pbAndjProperties = new HashMap<>();
    	        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
    	        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
    	        pbAndjProperties.put(PROPERTY_SALTSIZE, DEFAULT_SALT_SIZE);
    	        pbAndjProperties.put(PROPERTY_KEYSIZE, DEFAULT_KEY_SIZE);
    	        pbAndjPasswordHash.initialize(pbAndjProperties);
    	        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
    	        userForNewPerson.setPwHash(pwHash);
    	        userForNewPerson.setPerson(newPerson);
    	        SecurityRole userRole = em.createNamedQuery(ROLE_BY_NAME_QUERY, SecurityRole.class)
    	            .setParameter(PARAM1, USER_ROLE).getSingleResult();
    	        
    	        userForNewPerson.getRoles().add(userRole);
    	        userRole.getUsers().add(userForNewPerson);

    	        em.persist(userForNewPerson);
    	        return newPerson;
    	        
    	} catch (Exception ex) {
    		return null;
    	}
    }

    @Transactional
    public boolean deletePersonById(int id) {
        Person person = getById(id, GET_PERSON_BY_ID_QUERY_NAME, Person.class);
        if (person != null) {
            em.refresh(person);
            TypedQuery<SecurityUser> findUser = em
                .createNamedQuery(USER_FOR_OWNING_PERSON_QUERY, SecurityUser.class)
                .setParameter(PARAM1, person.getId());
            SecurityUser sUser = findUser.getSingleResult();
            person.getDonations().forEach((donation) -> em.remove(donation));
            em.remove(sUser);
            em.remove(person);
            return true;
        } else {
        	return false;
        }
    }
    

}