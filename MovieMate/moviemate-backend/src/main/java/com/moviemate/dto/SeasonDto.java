package com.moviemate.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SeasonDto extends SeasonSummaryDto {
    private List<EpisodeDto> episodes;
}
