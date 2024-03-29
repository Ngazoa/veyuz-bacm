package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.form.UserRoleForm;
import com.akouma.veyuzwebapp.model.AppRole;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.UserRole;
import com.akouma.veyuzwebapp.repository.UserRoleRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class UserRoleService {
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AppRoleService appRoleService;

    public UserRoleRepository getUserRoleRepository() {
        return userRoleRepository;
    }

    public void setUserRoleRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }


    public Iterable<UserRole> addUserRoles(UserRoleForm userRoleForm) {
        AppUser appUser = userRoleForm.getAppUser();
        if (appUser == null) {
            return null;
        }
        Iterable<UserRole> userRoles = userRoleRepository.findByAppUser(appUser);
        if (userRoles != null) {
            for (UserRole ur : userRoles) {
                userRoleRepository.deleteById(ur.getId());
            }
        }
        List<UserRole> roles = new ArrayList<>();
        if (userRoleForm.getAppRoles() != null) {
            for (AppRole appRole : userRoleForm.getAppRoles()) {
                UserRole userRole = userRoleRepository.findFirstByAppUserAndAppRole(userRoleForm.getAppUser(), appRole);
                if (userRole == null) {
                    userRole = new UserRole();
                    userRole.setAppRole(appRole);
                    userRole.setAppUser(userRoleForm.getAppUser());
                    userRoleForm.getAppUser().getUserRoles().add(userRole);
                    roles.add(userRole);
                }
            }
        }

        return userRoleRepository.saveAll(roles);
    }

    public UserRole getUserRole(AppUser appUser, AppRole role) {
        return userRoleRepository.findFirstByAppUserAndAppRole(appUser, role);
    }

    public UserRole saveUserRole(UserRole role) {
        return userRoleRepository.save(role);
    }
}
