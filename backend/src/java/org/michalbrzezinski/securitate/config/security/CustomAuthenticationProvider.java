package org.michalbrzezinski.securitate.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michalbrzezinski.securitate.config.security.port.DatabaseForSecurityConfiguration;
import org.michalbrzezinski.securitate.feature.security.events.CreateRoleSystemEvent;
import org.michalbrzezinski.securitate.feature.security.events.CreateUserSystemEvent;
import org.michalbrzezinski.securitate.feature.security.objects.Controller;
import org.michalbrzezinski.securitate.feature.security.objects.Role;
import org.michalbrzezinski.securitate.feature.security.objects.User;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
class CustomAuthenticationProvider {

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    private final DatabaseForSecurityConfiguration securityQueryService;
    private final SecurityConfigEventsPublisher applicationEventPublisher;

    public Collection<? extends GrantedAuthority> getUserAuthorities(DirContextOperations userData, String username) {
        log.info(">>>>> AUTHORIZATION START <<<<< [{}]", username);
        Set<Authority> authorities = new HashSet<>();
        String displayName = userData.getStringAttribute("displayName");
        String email = userData.getStringAttribute("mail");
        User user = getUser(email, displayName);
        setPermissions(user, authorities);
        log.info(">>>>> AUTHORIZATION END <<<<< [{}] granted for [{}]", username, authorities);
        return authorities;
    }

    private User getUser(String login, String displayName) {
        Optional<User> oUser = securityQueryService.getByLogin(login);
        if (oUser.isEmpty()) {
            log.debug("adding new user to database username [{}] ", displayName);
            return saveUser(login, displayName);
        }
        return oUser.get();
    }

    private User saveUser(String login, String displayName) {
        if (isFirstUser()) {
            log.info("first user");
            return saveNewUser(login, ADMIN, displayName);
        } else {
            log.info("next user");
            return saveNewUser(login, USER, displayName);
        }
    }

    private boolean isFirstUser() {
        return securityQueryService.countAll() == 0L;
    }

    private User saveNewUser(String login, String roleName, String displayName) {
        log.info("creating user account [{}]", login);
        String[] s = displayName.split(" ");
        User u = User.builder()
                .role(assignRole(roleName))
                .name(s[0])
                .surname(displayName)
                .login(login)
                .build();
        applicationEventPublisher.publish(CreateUserSystemEvent.builder()
                .created(ZonedDateTime.now())
                .payload(u)
                .build());
        return u;
    }

    private Role assignRole(String rolename) {
        log.info("assignRole [{}]", rolename);
        Optional<Role> oRole = securityQueryService.findRoleByName(rolename);
        if (oRole.isEmpty()) {
            log.info("creating role [{}]", rolename);
            Set<Controller> controllers = getControllerDOS(rolename);
            Role role = Role.builder().name(rolename).controllers(controllers).build();
            try {
                log.info("saving [{}]", role);
                applicationEventPublisher.publish(
                        CreateRoleSystemEvent.builder()
                                .created(ZonedDateTime.now())
                                .payload(role)
                                .build());
                return role;
            } catch (Exception e) {
                log.error(" error [{}]", e);
            }
        }
        return oRole.get();
    }

    private Set<Controller> getControllerDOS(String roleName) {
        if (ADMIN.equals(roleName))
            return securityQueryService.findAllControllers();
        else
            return new HashSet<>();
    }

    private void setPermissions(User user, Set<Authority> authorities) {
        securityQueryService.findControllersByUser(user).forEach(c -> addControllersToAuthorities(c, authorities));
    }

    private void addControllersToAuthorities(Controller c, Set<Authority> authorities) {
        log.info("addController [{}] to authorities [{}]", c, authorities);
        String stringified = StringifiyController.stringifyController(c.getController(), c.getMethod(), c.getHttp());
        log.info("stringified [{}] to ControllerDO [{}]", stringified, c);
        authorities.add(new Authority(stringified));
    }

    public static class Authority implements GrantedAuthority {
        public static final long serialVersionUID = 1L;
        private final String a;

        Authority(String a) {
            this.a = a;
        }

        @Override
        public String getAuthority() {
            return a;
        }
    }
}