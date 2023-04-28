package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.repository.AppRoleRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
public class AppRoleService {
    @Autowired
    private AppRoleRepository appRoleRepository;

    public AppRoleRepository getAppRoleRepository() {
        return appRoleRepository;
    }

    public void setAppRoleRepository(AppRoleRepository appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    public Iterable<AppRole> getRoles() {
        return appRoleRepository.findAll();
    }

    public AppRole getRoleByName(String roleName) {
        return appRoleRepository.findFirstByRoleName(roleName);
    }
}
