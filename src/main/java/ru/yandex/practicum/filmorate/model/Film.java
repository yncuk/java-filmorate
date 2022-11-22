package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class Film {
    int id;
    @NotNull @NotBlank String name;
    @NotNull
    String description;
    @NotNull
    LocalDate releaseDate;
    @NotNull
    Duration duration;
}
