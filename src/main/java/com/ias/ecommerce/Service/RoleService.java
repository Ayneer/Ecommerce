package com.ias.ecommerce.Service;

import com.ias.ecommerce.entity.Role;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.repository.RoleRepository;

public class RoleService {

    private RoleRepository roleRepository;

    public RoleService(){};

    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public Role getById(Integer id) {
        return roleRepository.findById(id).orElseThrow( () -> new DataNotFoundException("The Role with ID: "+id+" does not exist."));
    }

}
