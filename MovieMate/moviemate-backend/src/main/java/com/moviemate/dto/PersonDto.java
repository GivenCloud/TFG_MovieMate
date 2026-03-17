package com.moviemate.dto;

import lombok.Data;

@Data
public class PersonDto {
    private Integer id;
    private String name;
    private String biography;
    private String birthday;
    private String deathday;
    private String profileUrl;
    private String placeOfBirth;
    private String knownForDepartment;
}
