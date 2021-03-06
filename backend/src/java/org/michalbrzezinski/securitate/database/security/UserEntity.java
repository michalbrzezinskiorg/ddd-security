package org.michalbrzezinski.securitate.database.security;

import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"login"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String surname;
    private String login;
    private boolean active;
    @OneToMany(fetch = FetchType.LAZY, cascade = ALL)
    @JoinColumn(name = "permission_for")
    @Singular
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<PermissionEntity> permissions;
    @ManyToMany(fetch = FetchType.LAZY, cascade = ALL)
    @Singular
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<RoleEntity> roles;
}