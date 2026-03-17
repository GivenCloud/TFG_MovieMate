import api from './axios'
import type { SeasonSummary, SeasonDto } from '../types'

export const episodesApi = {
  /** Resumen de temporadas de una serie (sin episodios) */
  getTvSeasonsSummary: (tmdbId: number): Promise<SeasonSummary[]> =>
    api.get(`/tmdb/tv/${tmdbId}/seasons`).then((r) => r.data),

  /** Episodios de una temporada concreta */
  getSeasonDetails: (tmdbId: number, seasonNumber: number): Promise<SeasonDto> =>
    api.get(`/tmdb/tv/${tmdbId}/seasons/${seasonNumber}`).then((r) => r.data),

  /** Episodios vistos por el usuario para una serie (Set de "season-episode") */
  getWatchedEpisodes: (tmdbSeriesId: number): Promise<string[]> =>
    api.get(`/episodes/watched/${tmdbSeriesId}`).then((r) => r.data),

  /** Toggle: marca/desmarca un episodio visto. Devuelve el nuevo estado */
  toggleEpisodeWatched: (
    tmdbSeriesId: number,
    seasonNumber: number,
    episodeNumber: number
  ): Promise<boolean> =>
    api
      .post(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/${episodeNumber}`)
      .then((r) => r.data),

  /** Marca toda una temporada como vista */
  markSeasonWatched: (
    tmdbSeriesId: number,
    seasonNumber: number,
    episodeNumbers: number[]
  ): Promise<void> =>
    api
      .post(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/all`, episodeNumbers)
      .then(() => undefined),

  /** Desmarca toda una temporada */
  unmarkSeasonWatched: (tmdbSeriesId: number, seasonNumber: number): Promise<void> =>
    api
      .delete(`/episodes/watched/${tmdbSeriesId}/${seasonNumber}/all`)
      .then(() => undefined),
}
