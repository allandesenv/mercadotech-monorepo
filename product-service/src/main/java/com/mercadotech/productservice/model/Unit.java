package com.mercadotech.productservice.model;

import jakarta.persistence.*;

@Entity
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String abbreviation;
    private String description;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
