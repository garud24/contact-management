package com.himanshu.contact_management.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.himanshu.contact_management.dto.ContactRequest;
import com.himanshu.contact_management.dto.ContactResponse;
import com.himanshu.contact_management.entity.Contact;
import com.himanshu.contact_management.exception.ContactNotFoundException;
import com.himanshu.contact_management.exception.DuplicateEmailException;
import com.himanshu.contact_management.repository.ContactRepository;

@Service
public class ContactService {
    
    private final ContactRepository repository;

    public ContactService(ContactRepository repository)
    {
        this.repository = repository;
    }

    @Transactional
    public ContactResponse create(ContactRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        Contact contact = new Contact(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone()
        );

        Contact saved = repository.save(contact);
        return ContactResponse.from(saved);
    }

        @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));
        return ContactResponse.from(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional
    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        boolean emailChanged = !contact.getEmail().equals(request.email());
        if (emailChanged && repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());

        return ContactResponse.from(contact);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        repository.deleteById(id);
    }

}
