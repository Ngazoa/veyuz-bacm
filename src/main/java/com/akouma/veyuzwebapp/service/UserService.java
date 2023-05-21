package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.form.UserForm;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.AppUserDetails;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.repository.AppRoleRepositoryClass;
import com.akouma.veyuzwebapp.repository.UserRepository;
import com.akouma.veyuzwebapp.utils.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
     @Autowired
     FileStorageService fileStorageService;
    @Autowired
    private AppRoleRepositoryClass appRoleRepository;

    private static final String UPLOAD_DIR = "/avatar";

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppRoleRepositoryClass getAppRoleRepository() {
        return appRoleRepository;
    }

    public void setAppRoleRepository(AppRoleRepositoryClass appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Autowired
    private HttpServletRequest request;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AppUser appUser = userRepository.findFirstByEmail(username);
        System.out.println(appUser);
        if (appUser == null) {
            System.out.println("User not found! " + username);
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }
        System.out.println(appUser);
        List<String> roleNames = this.appRoleRepository.getRoleNames(appUser.getId());

        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        System.out.println("ROLE USER  sont vite CONNECTE ******************************************** : " + username + " --" + roleNames);

        if (roleNames != null) {
            for (String role : roleNames) {
                System.out.println("ROLE USER CONNECTE ******************************************** : " + role);
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role);
                grantedAuthorities.add(grantedAuthority);
            }
        }

        AppUserDetails appUserDetails = null;
        appUserDetails = new AppUserDetails(appUser);

        appUserDetails.addAuthorities(grantedAuthorities);

        return appUserDetails;
    }


    public AppUser saveUser(AppUser user) {
        return userRepository.save(user);
    }

    public AppUser getUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

    public AppUser getUserByUserName(String userName) {
        return userRepository.findFirstByUserName(userName);
    }

    public AppUser saveUser(UserForm userForm, HttpServletRequest request) throws IOException, ParseException {
        String encrytedPassword = null;
        if (userForm.getPassword() != null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            encrytedPassword = passwordEncoder.encode(userForm.getPassword());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(sdf.parse(userForm.getDateNaissanceStr()).getTime());

        Upload u = new Upload();
        String uploadRootPath = "avatar";
        System.out.println("uploadRootPath=" + uploadRootPath);

        String avatar = u.uploadFile(userForm.getAvatarFile(),fileStorageService, uploadRootPath,request);
        AppUser appUser = new AppUser();
        if (userForm.getUserId() != null) {
            appUser = userRepository.findById(userForm.getUserId()).get();
        }
        appUser.setNom(userForm.getNom());
        appUser.setAvatar(avatar);
        appUser.setEnable(false);
        appUser.setAdresse(userForm.getAdresse());
        appUser.setUserName(userForm.getUserName());
        appUser.setDateNaissance(date);
        appUser.setLieuNaissance(userForm.getLieuNaissance());
        appUser.setTelephone1(userForm.getTelephone1());
        appUser.setTelephone2(userForm.getTelephone2());
        appUser.setEmail(userForm.getEmail());
        appUser.setPassword(encrytedPassword);
        appUser.setPrenom(userForm.getPrenom());
        appUser.setGender(userForm.getGender());

        appUser.setBanque(userForm.getBanque());
        appUser.setAgence(userForm.getAgence());

        System.out.println("C'est Fait !");
        return saveUser(appUser);
    }


    public Iterable<AppUser> getBanqueUsers(Banque banque) {
        return userRepository.findByBanqueAndClientOrderByNomAsc(banque, null);
    }

    public void bloquerUserBank(AppUser appUser, boolean etat) {
        appUser.setEnable(etat);
        userRepository.save(appUser);
    }

    public Optional<AppUser> getAppUser(Long id) {
        return userRepository.findById(id);
    }

    public AppUser getLoggedUser(Principal principal) {
        AppUserDetails appUserDetails = (AppUserDetails) ((Authentication) principal).getPrincipal();
        AppUser loggedUser = this.getAppUser(appUserDetails.getId()).get();

        return loggedUser;
    }

    public boolean changeuserPassword(AppUser user, String oldPassword, String newpassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encrytedPassword = passwordEncoder.encode(oldPassword);
        boolean issamepassword=passwordEncoder.matches(oldPassword,user.getPassword());
        if (issamepassword) {
            user.setPassword(passwordEncoder.encode(newpassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
