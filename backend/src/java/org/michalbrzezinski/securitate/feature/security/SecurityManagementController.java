package org.michalbrzezinski.securitate.feature.security;

import lombok.RequiredArgsConstructor;
import org.michalbrzezinski.securitate.domain.security.ControllerDO;
import org.michalbrzezinski.securitate.domain.security.PermissionDO;
import org.michalbrzezinski.securitate.domain.security.RoleDO;
import org.michalbrzezinski.securitate.domain.security.UserDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("security")
public class SecurityManagementController {

    private final SecurityManegementService securityManagementService;

    @GetMapping("roles")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Page<RoleDO> getAllRoles(Pageable pageable) {
        return securityManagementService.getAllRoles(pageable);
    }

    @GetMapping("roles")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Page<UserDO> getAllUsers(Pageable pageable) {
        return securityManagementService.getAllUsers(pageable);
    }

    @GetMapping("roles")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Page<ControllerDO> getAllControllers(Pageable pageable) {
        return securityManagementService.getAllControllers(pageable);
    }

    @GetMapping("roles")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Page<PermissionDO> getAllPermissions(Pageable pageable) {
        return securityManagementService.getAllPermissions(pageable);
    }

    @PostMapping("user/permission")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void addPermission(PermissionDO permissionDO) {
        securityManagementService.addPermission(permissionDO);
    }

    @PostMapping("role/controller")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void addControllerToRole(RoleDO roleDO) {
        securityManagementService.addControllerToRole(roleDO);
    }

    @PostMapping("user/role")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void createRole(RoleDO roleDO) {
        securityManagementService.createRole(roleDO);
    }

    @PostMapping("role")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public void addRoleToUser(RoleDO roleDO) {
        securityManagementService.addRoleToUser(roleDO);
    }
}
