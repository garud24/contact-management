package com.himanshu.contact_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.himanshu.contact_management.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long>{

    boolean existsByEmail(String email);
    
}
