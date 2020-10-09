package com.ias.ecommerce.controller;

import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.repository.RoleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RoleController {

    private RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @PostMapping("/role")
    public Role addRole(@RequestParam(value = "name") String name,
                        @RequestParam(value = "description") String description){
        Role roleToSave = new Role(name, description);
        return roleRepository.save(roleToSave);
    }

    @GetMapping("/role/{id}")
    public Role getById(@PathVariable(value = "id") Integer id){
        return roleRepository.findById(id).orElseThrow();
    }

    @GetMapping("/role")
    public List<Role> getAll(){
        List<Role> roleList = new ArrayList<>();
        roleRepository.findAll().forEach(role -> roleList.add(role));
        return roleList;
    }

}
