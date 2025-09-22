package com.example.shbank.entity;


import com.example.shbank.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    public void updateInfo(@NotBlank String name, @Email @NotBlank String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름");
        }
    }

    public void updateEmail(@Email @NotBlank String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
