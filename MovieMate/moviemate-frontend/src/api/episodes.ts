import apiClient from '../lib/apiClient'
import type { SeasonSummary, SeasonDto, SeriesProgressDto } from '../types'

export const episodesApi = {
  /** Resumen de temporadas de una serie (sin episodios) */
  getTvSeasonsSummary: (tmdbId: number): Promise<SeasonSummary[]> =>
    apiClient.get(`/tmdb/tv/${tmdbId}/seasons`).then((r: { data: SeasonSummary[] }) => r.data),

  /** Episodios de una temporada concreta */
  getSeasonDetails: (tmdbId: number, seasonNumber: number): Promise<SeasonDto> =>
    apiClient.get(`/tmdb/tv/${tmdbId}/seasons/${seasonNumber}`).then((r: { data: SeasonDto }) => r.data),

  /** Episodios vistos por el usuario para una serie (Set de "season-episode") */
  getWatchedEpisodes: (tmdbSeriesId: number): Promise<string[]> =>
    apiClient.get(`/episodes/watched/${tmdbSeriesId}`).then((r: { data: string[] }) => r.data),

  /** Toggle: marca/desmarca un episodio visto. Devuelve el nuevo estado */
  toggleEpisodeWatched: (
    tmdbSeriesId: number,
    seasonNumber: number,
    episodeNumber: number
  ): Promise<boolean> =>
    apiClient
      .post(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/${episodeNumber}`)
      .then((r: { data: boolean }) => r.data),

  /** Marca toda una temporada como vista */
  markSeasonWatched: (
    tmdbSeriesId: number,
    seasonNumber: number,
    episodeNumbers: number[]
  ): Promise<void> =>
    apiClient
      .post(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/all`, episodeNumbers)
      .then(() => undefined),

  /** Desmarca toda una temporada */
  unmarkSeasonWatched: (tmdbSeriesId: number, seasonNumber: number): Promise<void> =>
    apiClient
      .delete(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/all`)
      .then(() => undefined),

  /** Progreso de episodios vistos agrupado por serie */
  getSeriesProgress: (): Promise<SeriesProgressDto[]> =>
    apiClient.get('/episodes/watched/summary').then((r: { data: SeriesProgressDto[] }) => r.data),
}
