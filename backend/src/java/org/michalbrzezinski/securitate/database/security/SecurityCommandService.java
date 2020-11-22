package org.michalbrzezinski.securitate.database.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.michalbrzezinski.securitate.domain.security.objects.ControllerDO;
import org.michalbrzezinski.securitate.domain.security.objects.PermissionDO;
import org.michalbrzezinski.securitate.domain.security.objects.RoleDO;
import org.michalbrzezinski.securitate.domain.security.objects.UserDO;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class SecurityCommandService {

    private final ControllerRepository controllerRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final CustomEntityMapper customEntityMapper;


    void save(ControllerDO controllerDO) {
        log.info("save [{}]", controllerDO);
        try {
            log.info("found controller [{}]", controllerDO);
            Controller controller = customEntityMapper.map(controllerDO);
            log.info("mapped controller [{}]", controller);
            controllerRepository.save(controller);
        } catch (Exception e) {
            log.warn("found the same controller several times - NO ISSUE [{}]", e);
        }
    }

    UserDO save(UserDO user) {
        log.info("save [{}]", user);
        User u = customEntityMapper.map(user);
        saveRoles(user.getRoles());
        savePermissionDO(user.getPermissions());
        Set<Role> roles = getRoles(user);
        Set<Permission> permissions = getPermissions(user);
        u.setRoles(roles);
        u.setPermissions(permissions);
        return customEntityMapper.map(userRepository.save(u));
    }

    RoleDO save(RoleDO role) {
        log.info("save [{}]", role);
        Role r = customEntityMapper.map(role);
        Set<Controller> roles = getControllersForRole(role);
        r.setControllers(roles);
        return customEntityMapper.map(save(r));
    }


    private Set<Role> getRoles(UserDO u) {
        log.info("getRoles for user [{}]", u);
        Set<Role> roles = u.getRoles().stream()
                .map(r -> roleRepository.findById(r.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        log.info("found Roles [{}]", roles);
        return roles;
    }

    private Set<Permission> getPermissions(UserDO u) {
        log.info("getPermissions for user [{}]", u);
        Set<Permission> permissions = u.getPermissions()
                .stream().map(p -> permissionRepository.findById(p.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        log.info("found Permissions [{}]", permissions);
        return permissions;
    }


    private Set<Controller> getControllersForRole(RoleDO role) {
        log.info("getControllersForRole [{}]", role);
        Set<Controller> controllers = role.getControllers().stream()
                .map(c -> controllerRepository.findById(c.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        log.info("found Controllers [{}]", controllers);
        return controllers;
    }

    private void savePermissionDO(Set<PermissionDO> permissions) {
        log.info("savePermissionDO [{}]", permissions);
        Set<Permission> perms = permissions.stream()
                .map(p -> save(p))
                .collect(Collectors.toSet());
        log.info("permissions saved [{}]", perms);
    }

    private Permission save(PermissionDO p) {
        log.info("save [{}]", p);
        Permission saved = permissionRepository.save(customEntityMapper.map(p));
        log.info("saved [{}]", saved);
        return saved;
    }

    private void saveRoles(Set<RoleDO> roles) {
        log.info("save [{}]", roles);
        roles.stream()
                .filter(r -> roleRepository.findByName(r.getName()).isEmpty())
                .map(r -> save(customEntityMapper.map(r)))
                .peek(r -> r.setControllers(getControllers(r)))
                .collect(Collectors.toSet());
    }

    private Set<Controller> getControllers(Role r) {
        log.info("save [{}]", r);
        return r.getControllers().stream()
                .map(c -> controllerRepository.findById(c.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Role save(Role role) {
        log.info("save [{}]", role);
        return roleRepository.save(role);
    }

    public PermissionDO addPermission(PermissionDO permission) {
        log.info("creating permission [{}]", permission);
        Collection<Controller> controllers = controllerRepository.findByIdIn(permission.getControllers().stream().map(ControllerDO::getId).collect(Collectors.toList()));
        User createdFor = userRepository.getOne(permission.getPermissionFor().getId());
        User createdBy = userRepository.getOne(permission.getCreatedBy().getId());
        Permission permissionForUser = customEntityMapper.map(permission, createdBy, createdFor, controllers);
        log.info("saving mapped values stored in PermissionDO [{}]", permissionForUser);
        return customEntityMapper.map(permissionRepository.save(permissionForUser));
    }

    public Optional<RoleDO> addControllerToRole(RoleDO roleDO) {
        Optional<Role> role = roleRepository.findById(roleDO.getId());
        role.ifPresent(r -> addControllerToRole(r, roleDO.getControllers()));
        Optional<RoleDO> result = role.map(customEntityMapper::map);
        return result;
    }

    private void addControllerToRole(Role r, Set<ControllerDO> controllersDO) {
        List<Controller> controllers = controllerRepository.findByIdIn(controllersDO.stream().map(c -> c.getId()).collect(Collectors.toList()));
        r.setControllers(new HashSet<>(controllers));
        roleRepository.save(r);
    }

    public RoleDO saveNewRoleCreatedByUserEvent(RoleDO roleDO) {
        return customEntityMapper.map(roleRepository.save(customEntityMapper.map(roleDO)));
    }
}