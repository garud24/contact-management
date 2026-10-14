package com.himanshu.contact_management.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.himanshu.contact_management.dto.ContactRequest;
import com.himanshu.contact_management.dto.ContactResponse;
import com.himanshu.contact_management.service.ContactService;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@RequestBody ContactRequest request) {
        ContactResponse created = contactService.create(request);
        URI location = URI.create("/api/contacts/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

        @GetMapping
    public List<ContactResponse> getAll() {
        return contactService.getAll();
    }

    @GetMapping("/{id}")
    public ContactResponse getById(@PathVariable Long id) {
        return contactService.getById(id);
    }

        @PutMapping("/{id}")
    public ContactResponse update(@PathVariable Long id,
                                  @RequestBody ContactRequest request) {
        return contactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
