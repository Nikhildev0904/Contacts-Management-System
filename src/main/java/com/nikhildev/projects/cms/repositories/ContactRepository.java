package com.cognitree.internship.cms.repositories;

import com.cognitree.internship.cms.models.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends MongoRepository<Contact, String> {

    Optional<Contact> findByPhone(String phone);

    Page<Contact> findByContactNameContainingIgnoreCase(String contactName, Pageable pageable);

    Page<Contact> findByPhoneContaining(String phone, Pageable pageable);

    Page<Contact> findByCategoryIdsIn(String categoryId, Pageable pageable);

    Page<Contact> findByCategoryIdsInAndContactNameContainingIgnoreCase(String categoryId, String contactName, Pageable pageable);

    Page<Contact> findByCategoryIdsInAndPhoneContaining(String categoryId, String phone, Pageable pageable);

    @Query("{'categoryIds': {$in: ?0}}")
    Page<Contact> findByCategoryIdsInList(List<String> categoryIds, Pageable pageable);
}