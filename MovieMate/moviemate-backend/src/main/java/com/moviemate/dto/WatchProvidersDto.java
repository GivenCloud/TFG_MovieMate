package com.moviemate.dto;

import lombok.Data;
import java.util.List;

@Data
public class WatchProvidersDto {
    /** URL de JustWatch para este contenido y país */
    private String link;
    /** Plataformas de streaming (flatrate) */
    private List<ProviderDto> flatrate;
    /** Opciones de alquiler */
    private List<ProviderDto> rent;
    /** Opciones de compra */
    private List<ProviderDto> buy;

    @Data
    public static class ProviderDto {
        private Integer providerId;
        private String providerName;
        private String logoUrl;
    }
}
