package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import lombok.Data;

import java.util.Collection;

@Data
public class UserRoleForm {
    private Collection<AppRole> appRoles;
    private AppUser appUser;

    public Collection<AppRole> getAppRoles() {
        return appRoles;
    }

    public void setAppRoles(Collection<AppRole> appRoles) {
        this.appRoles = appRoles;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }
}
