package com.poc.alerts.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poc.alerts.entity.PayloadMst;
import com.poc.alerts.repository.PayloadRepository;

@RestController
@RequestMapping("/payload")
public class PayloadController {

    private final PayloadRepository repo;

    public PayloadController(PayloadRepository repo){
        this.repo = repo;
    }

    @PostMapping
    public PayloadMst insert(@RequestBody PayloadMst payload){
        return repo.save(payload);
    }

    @GetMapping
    public Object getAll(){
        return repo.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        repo.deleteById(id);
    }
}
