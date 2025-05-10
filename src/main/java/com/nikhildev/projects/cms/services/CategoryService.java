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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final ContactRepository contactRepository;
    private final TenantContext tenantContext;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           ContactRepository contactRepository,
                           TenantContext tenantContext) {
        this.categoryRepository = categoryRepository;
        this.contactRepository = contactRepository;
        this.tenantContext = tenantContext;
    }

    public PagedResponse<Category> getAllCategories(String categoryName, int page, int size,
                                                    String sortBy, Sort.Direction sortOrder) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching categories with name: {}, page: {}", tenantId, categoryName, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder, sortBy));
        Page<Category> categoriesPage;
        if (categoryName != null && !categoryName.isEmpty()) {
            logger.debug("[Tenant: {}] Searching categories by name: {}", tenantId, categoryName);
            List<Category> categoryList = categoryRepository.findByCategoryNameContainingIgnoreCase(categoryName);
            categoriesPage = new PageImpl<>(categoryList, pageable, categoryList.size());
        } else {
            logger.debug("[Tenant: {}] Fetching all categories", tenantId);
            categoriesPage = categoryRepository.findAll(pageable);
        }
        logger.debug("[Tenant: {}] Found {} categories", tenantId, categoriesPage.getTotalElements());
        return PagedResponse.fromPage(categoriesPage);
    }

    public Category getCategoryById(String categoryId) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching category by ID: {}", tenantId, categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    logger.error("[Tenant: {}] Category not found with ID: {}", tenantId, categoryId);
                    return new ResourceNotFoundException("Category not found with id: " + categoryId);
                });
        logger.debug("[Tenant: {}] Found category: {}", tenantId, category.getCategoryName());
        return category;
    }

    public Category createCategory(Category category) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Creating new category with name: {}", tenantId, category.getCategoryName());
        if (categoryRepository.existsByCategoryNameIgnoreCase(category.getCategoryName())) {
            logger.error("[Tenant: {}] Category already exists with name: {}", tenantId, category.getCategoryName());
            throw new ResourceAlreadyExistsException("Category with name: " + category.getCategoryName() + " already exists");
        }
        Category savedCategory = categoryRepository.save(category);
        logger.info("[Tenant: {}] Created new category with ID: {}", tenantId, savedCategory.getId());
        return savedCategory;
    }

    public Category updateCategory(String categoryId, Category categoryDetails) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Updating category with ID: {}", tenantId, categoryId);
        Category existingCategory = getCategoryById(categoryId);
        if (categoryDetails.getCategoryName() != null &&
                !existingCategory.getCategoryName().equalsIgnoreCase(categoryDetails.getCategoryName()) &&
                categoryRepository.existsByCategoryNameIgnoreCase(categoryDetails.getCategoryName())) {
            logger.error("[Tenant: {}] Cannot update category. Name already exists: {}",
                    tenantId, categoryDetails.getCategoryName());
            throw new ResourceAlreadyExistsException("Category with name: " +
                    categoryDetails.getCategoryName() + " already exists");
        }
        existingCategory.updateFrom(categoryDetails);
        Category updatedCategory = categoryRepository.save(existingCategory);
        logger.info("[Tenant: {}] Updated category with ID: {}", tenantId, categoryId);
        return updatedCategory;
    }

    public void deleteCategory(String categoryId) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Attempting to delete category with ID: {}", tenantId, categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            logger.error("[Tenant: {}] Cannot delete category. Not found with ID: {}", tenantId, categoryId);
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
        logger.info("[Tenant: {}] Deleted category with ID: {}", tenantId, categoryId);
    }

    public PagedResponse<Contact> getCategoryContacts(String categoryId, String contactName, String phone,
                                                      int page, int size, String sortBy, Sort.Direction sortOrder) {
        String tenantId = tenantContext.getTenantId();
        logger.debug("[Tenant: {}] Fetching contacts for category: {}, name: {}, phone: {}, page: {}",
                tenantId, categoryId, contactName, phone, page);
        getCategoryById(categoryId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder, sortBy));
        Page<Contact> contactsPage;
        if (contactName != null && !contactName.isEmpty()) {
            logger.debug("[Tenant: {}] Searching contacts by name: {} in category: {}", tenantId, contactName, categoryId);
            contactsPage = contactRepository.findByCategoryIdsInAndContactNameContainingIgnoreCase(categoryId, contactName, pageable);
        } else if (phone != null && !phone.isEmpty()) {
            logger.debug("[Tenant: {}] Searching contacts by phone: {} in category: {}", tenantId, phone, categoryId);
            contactsPage = contactRepository.findByCategoryIdsInAndPhoneContaining(categoryId, phone, pageable);
        } else {
            logger.debug("[Tenant: {}] Fetching all contacts in category: {}", tenantId, categoryId);
            contactsPage = contactRepository.findByCategoryIdsIn(categoryId, pageable);
        }
        logger.debug("[Tenant: {}] Found {} contacts in category {}", tenantId, contactsPage.getTotalElements(), categoryId);
        return PagedResponse.fromPage(contactsPage);
    }
}
