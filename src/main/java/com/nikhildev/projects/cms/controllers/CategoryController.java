package com.cognitree.internship.cms.controllers;

import com.cognitree.internship.cms.models.Category;
import com.cognitree.internship.cms.models.Contact;
import com.cognitree.internship.cms.models.PagedResponse;
import com.cognitree.internship.cms.services.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/categories")
@PreAuthorize("hasRole('USER')")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Category>> getAllCategories(
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortOrder) {
        logger.info("Fetching categories with categoryName={}, page={}, pageSize={}, sortBy={}, sortOrder={}",
                categoryName, page, pageSize, sortBy, sortOrder);
        PagedResponse<Category> response = categoryService.getAllCategories(categoryName, page, pageSize, sortBy, sortOrder);
        logger.debug("Fetched {} categories", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        logger.info("Creating new category with name: {}", category.getCategoryName());
        Category createdCategory = categoryService.createCategory(category);
        logger.info("Created category with ID: {}", createdCategory.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String categoryId) {
        logger.info("Fetching category with ID: {}", categoryId);
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable String categoryId,
            @RequestBody Category category) {
        logger.info("Updating category with ID: {}", categoryId);
        Category updatedCategory = categoryService.updateCategory(categoryId, category);
        logger.info("Successfully updated category: {}", categoryId);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        logger.info("Deleting category with ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        logger.info("Successfully deleted category: {}", categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/contacts")
    public ResponseEntity<PagedResponse<Contact>> getCategoryContacts(
            @PathVariable String categoryId,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "contactName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortOrder) {
        logger.info("Fetching contacts for category ID: {} with filters: contactName={}, phone={}",
                categoryId, contactName, phone);
        PagedResponse<Contact> contacts = categoryService.getCategoryContacts(
                categoryId, contactName, phone, page, pageSize, sortBy, sortOrder);
        logger.debug("Found {} contacts for category {}", contacts.getTotalElements(), categoryId);
        return ResponseEntity.ok(contacts);
    }
}