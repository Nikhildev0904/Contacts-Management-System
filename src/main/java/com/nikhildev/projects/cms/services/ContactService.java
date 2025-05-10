package com.cognitree.internship.cms.services;

import com.cognitree.internship.cms.config.TenantContext;
import com.cognitree.internship.cms.exceptions.ResourceAlreadyExistsException;
import com.cognitree.internship.cms.exceptions.ResourceNotFoundException;
import com.cognitree.internship.cms.models.Category;
import com.cognitree.internship.cms.models.Contact;
import com.cognitree.internship.cms.models.PagedResponse;
import com.cognitree.internship.cms.repositories.CategoryRepository;
import com.cognitree.internship.cms.repositories.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final CategoryRepository categoryRepository;
    private final TenantContext tenantContext;

    @Autowired
    public ContactService(ContactRepository contactRepository,
                          CategoryRepository categoryRepository,
                          TenantContext tenantContext) {
        this.contactRepository = contactRepository;
        this.categoryRepository = categoryRepository;
        this.tenantContext = tenantContext;
    }

    public PagedResponse<Contact> getAllContacts(
            String contactName, String phone, String categoryName,
            int page, int size, String sortBy, Sort.Direction sortOrder) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching contacts with filters - name: {}, phone: {}, categoryName: {}, page: {}",
                tenantId, contactName, phone, categoryName, page);
        Sort sort = Sort.by(sortOrder, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Contact> contacts;
        if (contactName != null && !contactName.isEmpty()) {
            logger.debug("[Tenant: {}] Searching contacts by name: {}", tenantId, contactName);
            contacts = contactRepository.findByContactNameContainingIgnoreCase(contactName, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            logger.debug("[Tenant: {}] Searching contacts by phone: {}", tenantId, phone);
            contacts = contactRepository.findByPhoneContaining(phone, pageable);
        } else if (categoryName != null && !categoryName.isEmpty()) {
            logger.debug("[Tenant: {}] Searching contacts by category name: {}", tenantId, categoryName);
            List<Category> categories = categoryRepository.findByCategoryNameContainingIgnoreCase(categoryName);
            if (categories.isEmpty()) {
                return PagedResponse.fromPage(Page.empty(pageable));
            }
            List<String> categoryIds = categories.stream().map(Category::getId).toList();
            contacts = contactRepository.findByCategoryIdsInList(categoryIds, pageable);
        } else {
            logger.debug("[Tenant: {}] Fetching all contacts", tenantId);
            contacts = contactRepository.findAll(pageable);
        }
        logger.debug("[Tenant: {}] Found {} contacts", tenantId, contacts.getTotalElements());
        return PagedResponse.fromPage(contacts);
    }

    public Contact createContact(Contact contact) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Creating new contact with phone: {}", tenantId, contact.getPhone());
        Optional<Contact> existingContact = contactRepository.findByPhone(contact.getPhone());
        if (existingContact.isPresent()) {
            logger.error("[Tenant: {}] Contact already exists with phone: {}", tenantId, contact.getPhone());
            throw new ResourceAlreadyExistsException("Contact with phone number " + contact.getPhone() + " already exists");
        }
        // Validate category IDs if provided
        if (contact.getCategoryIds() != null && !contact.getCategoryIds().isEmpty()) {
            List<String> categoryIds = contact.getCategoryIds();
            for (String categoryId : categoryIds) {
                if (!categoryRepository.existsById(categoryId)) {
                    logger.error("[Tenant: {}] Category not found with ID: {}", tenantId, categoryId);
                    throw new ResourceNotFoundException("Category not found with id: " + categoryId);
                }
            }
            logger.debug("[Tenant: {}] Assigning {} categories to new contact", tenantId, categoryIds.size());
        }
        Contact savedContact = contactRepository.save(contact);
        logger.info("[Tenant: {}] Created new contact with ID: {}", tenantId, savedContact.getId());
        return savedContact;
    }

    public Contact getContactById(String id) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching contact by ID: {}", tenantId, id);
        return findContactById(id);
    }

    public Contact updateContact(String id, Contact contactDetails) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Updating contact with ID: {}", tenantId, id);
        Contact existingContact = findContactById(id);
        // Check if phone is being updated and if it already exists
        if (contactDetails.getPhone() != null &&
                !existingContact.getPhone().equals(contactDetails.getPhone())) {
            logger.debug("[Tenant: {}] Updating phone number for contact: {}", tenantId, id);
            Optional<Contact> contactWithPhone = contactRepository.findByPhone(contactDetails.getPhone());
            if (contactWithPhone.isPresent() && !contactWithPhone.get().getId().equals(id)) {
                logger.error("[Tenant: {}] Phone number already exists: {}", tenantId, contactDetails.getPhone());
                throw new ResourceAlreadyExistsException("Contact with phone number " +
                        contactDetails.getPhone() + " already exists");
            }
        }
        if (contactDetails.getCategoryIds() != null && !contactDetails.getCategoryIds().isEmpty()) {
            for (String categoryId : contactDetails.getCategoryIds()) {
                if (!categoryRepository.existsById(categoryId)) {
                    logger.error("[Tenant: {}] Category not found with ID: {}", tenantId, categoryId);
                    throw new ResourceNotFoundException("Category not found with id: " + categoryId);
                }
            }
            logger.debug("[Tenant: {}] Updating categories for contact: {}", tenantId, id);
        }
        existingContact.updateFrom(contactDetails);
        Contact updatedContact = contactRepository.save(existingContact);
        logger.info("[Tenant: {}] Updated contact with ID: {}", tenantId, id);
        return updatedContact;
    }

    public void deleteContact(String contactId) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Attempting to delete contact with ID: {}", tenantId, contactId);
        if (!contactRepository.existsById(contactId)) {
            logger.error("[Tenant: {}] Contact not found with ID: {}", tenantId, contactId);
            throw new ResourceNotFoundException("Contact not found with contactId: " + contactId);
        }
        Contact contact = findContactById(contactId);
        contactRepository.delete(contact);
        logger.info("[Tenant: {}] Deleted contact with ID: {}", tenantId, contactId);
    }

    public PagedResponse<Category> getContactCategories(
            String contactId,
            String categoryName,
            int page, int pageSize,
            String sortBy, Sort.Direction sortOrder
    ) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching categories for contact: {}, name filter: {}, page: {}",
                tenantId, contactId, categoryName, page);
        Contact contact = getContactById(contactId);
        List<String> categoryIds = contact.getCategoryIds();
        if (categoryIds.isEmpty()) {
            logger.debug("[Tenant: {}] No categories found for contact: {}", tenantId, contactId);
            return PagedResponse.fromPage(Page.empty());
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortOrder, sortBy));
        Page<Category> categoriesPage;
        if (StringUtils.hasText(categoryName)) {
            logger.debug("[Tenant: {}] Searching categories by name: {} for contact: {}", tenantId, categoryName, contactId);
            categoriesPage = categoryRepository
                    .findByIdInAndCategoryNameContainingIgnoreCase(
                            categoryIds, categoryName, pageable);
        } else {
            logger.debug("[Tenant: {}] Fetching all categories for contact: {}", tenantId, contactId);
            categoriesPage = categoryRepository
                    .findByIdIn(categoryIds, pageable);
        }
        logger.debug("[Tenant: {}] Found {} categories for contact {}", tenantId, categoriesPage.getTotalElements(), contactId);
        return PagedResponse.fromPage(categoriesPage);
    }

    public Contact addCategoryToContact(String contactId, String categoryId) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Adding category {} to contact {}", tenantId, categoryId, contactId);
        Contact contact = getContactById(contactId);
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    logger.error("[Tenant: {}] Category not found with ID: {}", tenantId, categoryId);
                    return new ResourceNotFoundException("Category not found");
                });
        boolean categoryExists = contact.getCategoryIds().contains(categoryId);
        if (!categoryExists) {
            contact.getCategoryIds().add(categoryId);
            contact = contactRepository.save(contact);
            logger.info("[Tenant: {}] Added category {} to contact {}", tenantId, categoryId, contactId);
        } else {
            logger.debug("[Tenant: {}] Category {} already assigned to contact {}", tenantId, categoryId, contactId);
        }
        return contact;
    }

    public void removeCategoryFromContact(String contactId, String categoryId) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Removing category {} from contact {}", tenantId, categoryId, contactId);
        Contact contact = getContactById(contactId);
        boolean removed = contact.getCategoryIds().remove(categoryId);
        if (!removed) {
            logger.error("[Tenant: {}] Category {} not associated with contact {}", tenantId, categoryId, contactId);
            throw new ResourceNotFoundException("Category not associated with this contact");
        }
        contactRepository.save(contact);
        logger.info("[Tenant: {}] Removed category {} from contact {}", tenantId, categoryId, contactId);
    }

    private Contact findContactById(String id) {
        String tenantId = tenantContext.getTenantId();
        return contactRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("[Tenant: {}] Contact not found with ID: {}", tenantId, id);
                    return new ResourceNotFoundException("Contact not found with id: " + id);
                });
    }
}