package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "clients")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Client extends User {
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false)
    @Builder.Default
    @ColumnDefault("true")
    private Boolean active = true;
    @ManyToMany
    @JoinTable(
            name = "baskets",
            joinColumns = { @JoinColumn(name = "client_id") },
            inverseJoinColumns = { @JoinColumn(name = "book_id") }
    )
    Set<Book> booksInBasket;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public Client(Long id, String email, String password, String name, BigDecimal balance) {
        super(id, email, password, name);
        this.balance = balance;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }

    @Override
    public boolean isBlocked() {
        return !active;
    }
}
