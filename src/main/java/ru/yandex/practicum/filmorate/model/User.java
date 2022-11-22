package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class User {
    int id;
    @NotNull @Email
    String email;
    @NotNull @NotBlank
    String login;
    String name;
    @NotNull
    LocalDate birthday;
}
