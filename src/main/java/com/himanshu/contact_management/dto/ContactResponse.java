package com.himanshu.contact_management.dto;

import java.time.Instant;

import com.himanshu.contact_management.entity.Contact;
public record ContactResponse (Long id, String firstName, String lastName, String email, String phone, Instant createdAt, Instant updatedAt) {

    public static ContactResponse from (Contact contact)
    {
        return new ContactResponse(contact.getId(), contact.getFirstName(), contact.getLastName(), contact.getEmail(), contact.getPhone(), contact.getCreatedAt(), contact.getUpdatedAt());
    }
    
}
