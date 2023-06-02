package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import lombok.Data;

import java.util.Set;


@Data
public class UserRoleForm {
    private Set<AppRole> appRoles;
    private AppUser appUser;

}
