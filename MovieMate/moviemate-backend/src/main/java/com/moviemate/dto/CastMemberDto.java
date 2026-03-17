package com.moviemate.dto;

import lombok.Data;

@Data
public class CastMemberDto {
    private Integer personId;
    private String name;
    private String profileUrl;
    /** Para actores: papel que interpreta */
    private String character;
    /** Para crew: cargo (Director, Screenplay…) */
    private String job;
    private String department;
}
