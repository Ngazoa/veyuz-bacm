package com.akouma.veyuzwebapp.configuration;

import com.akouma.veyuzwebapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity

@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void ConfigureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService).passwordEncoder(this.passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable();

        http.authorizeRequests().antMatchers(
                "/login",
                "/logout",
                "/resources/**", "sweetalert2/**", "/css/**",
                "/css/*", "/static/**" , "jquery/*", "/sweetalert2themebootstrap-4/**",
                "/js/*", "/img/*","/fonts/*", "/img/avatars/*", "/img/icons/*",
                "/photos/*", "/upload/**", "sweetalert2/*", "/upload/avatar/clients/*",
                "/upload/avatar/*", "/upload/fichiers_transactions/*"
            ).permitAll();

        http.authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginProcessingUrl("/j_spring_security_check")
            .loginPage("/login")
            .defaultSuccessUrl("/",true )
            .failureUrl("/login?error=true")
            .usernameParameter("email")
            .passwordParameter("password")
            .and().logout().logoutUrl("/logout").logoutSuccessUrl("/login?logout")
        ;
        // Config Remember Me.
//        http.authorizeRequests()
//            .and() //
//            .rememberMe().tokenRepository(this.persistentTokenRepository()) //
//            .tokenValiditySeconds(365 * 24 * 60 * 60); // 24h

    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }


}
