package com.cognitree.internship.cms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contacts")
public class Contact {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Phone number is required")
    @Indexed(unique = true)
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private List<String> categoryIds = new ArrayList<>();

    public void updateFrom(Contact source) {
        if (source.getContactName() != null) {
            this.contactName = source.getContactName();
        }
        if (source.getPhone() != null) {
            this.phone = source.getPhone();
        }
        if (source.getEmail() != null) {
            this.email = source.getEmail();
        }
        if (source.getCategoryIds() != null) {
            this.categoryIds = source.getCategoryIds();
        }
    }
}