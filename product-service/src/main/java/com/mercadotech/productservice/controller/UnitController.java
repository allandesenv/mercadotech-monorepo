package com.mercadotech.productservice.controller;

import com.mercadotech.productservice.model.Unit;
import com.mercadotech.productservice.repository.UnitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/units")
public class UnitController {
    private final UnitRepository repository;

    public UnitController(UnitRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Unit> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public Unit create(@RequestBody Unit unit) {
        return repository.save(unit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
