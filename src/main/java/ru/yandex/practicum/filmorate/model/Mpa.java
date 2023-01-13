package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class Mpa {
    @NotNull Integer id;
    String name;

    @JsonCreator
    public Mpa(@JsonProperty("id") Integer id,
               @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }
}
