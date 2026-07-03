package dev.rudyevhenii.crypto_aggregator.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(name = UserEntity.TABLE_NAME)
public class UserEntity implements Persistable<UUID>, UserDetails {
    public static final String TABLE_NAME = "users";

    @Id
    @Column(name = Fields.id)
    private UUID id;

    @Column(name = Fields.email)
    private String email;

    @Column(name = Fields.password)
    private String password;

    @Column(name = Fields.firstName)
    private String firstName;

    @Column(name = Fields.lastName)
    private String lastName;

    @Transient
    private boolean newEntity;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
