package com.cognitree.internship.cms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {

    @Id
    @JsonProperty(access = Access.READ_ONLY)
    private String id;

    @NotBlank(message = "Category name is required")
    @Indexed(unique = true)
    private String categoryName;

    private String description;

    public void updateFrom(Category source) {
        if (source.getCategoryName() != null) {
            this.categoryName = source.getCategoryName();
        }
        if (source.getDescription() != null) {
            this.description = source.getDescription();
        }
    }
}