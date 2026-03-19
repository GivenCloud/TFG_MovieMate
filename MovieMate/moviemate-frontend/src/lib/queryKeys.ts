// Todas las claves de React Query en un único lugar
// Patrón: de más general a más específico
export const queryKeys = {
  // TMDB — búsqueda y populares (datos directos de TMDB, sin BD)
  tmdb: {
    popularMovies: () => ['tmdb', 'popular', 'movies'] as const,
    popularTv:     () => ['tmdb', 'popular', 'tv'] as const,
    trending:      () => ['tmdb', 'trending'] as const,
    searchMovies:  (q: string) => ['tmdb', 'search', 'movies', q] as const,
    searchTv:      (q: string) => ['tmdb', 'search', 'tv', q] as const,
    sync: (tmdbId: number, type: string) => ['tmdb', 'sync', type, tmdbId] as const,
    genresMovies:  () => ['tmdb', 'genres', 'movies'] as const,
    genresTv:      () => ['tmdb', 'genres', 'tv'] as const,
    discoverMovies:   (params: object) => ['tmdb', 'discover', 'movies', params] as const,
    discoverTv:       (params: object) => ['tmdb', 'discover', 'tv', params] as const,
    watchProviders:   (tmdbId: number, type: string) => ['tmdb', 'providers', type, tmdbId] as const,
    person:           (id: number) => ['tmdb', 'person', id] as const,
    personCredits:    (id: number) => ['tmdb', 'person', id, 'credits'] as const,
    contentCredits:   (tmdbId: number, type: string) => ['tmdb', type, tmdbId, 'credits'] as const,
    tvSeasons:        (tmdbId: number) => ['tmdb', 'tv', tmdbId, 'seasons'] as const,
    seasonDetail:     (tmdbId: number, season: number) => ['tmdb', 'tv', tmdbId, 'seasons', season] as const,
  },
  // Episodios vistos (por usuario autenticado)
  episodes: {
    watched:  (tmdbSeriesId: number) => ['episodes', 'watched', tmdbSeriesId] as const,
    progress: () => ['episodes', 'watched', 'summary'] as const,
  },
  // Contenido en BD (caché local — ficha de detalle)
  content: {
    all:    ['content'] as const,
    detail: (id: number) => ['content', id] as const,
  },
  // Usuario
  users: {
    all:         ['users'] as const,
    me:          () => ['users', 'me'] as const,
    byUsername:  (username: string) => ['users', 'username', username] as const,
    profileById: (id: number) => ['users', id] as const,
    stats:       (id: number) => ['users', id, 'stats'] as const,
    fullStats:       () => ['users', 'me', 'stats', 'full'] as const,
    recommendations: () => ['users', 'me', 'recommendations'] as const,
    ratings:        () => ['users', 'me', 'ratings'] as const,
    lists:          () => ['users', 'me', 'lists'] as const,
    ratingsByUser:  (id: number) => ['users', id, 'ratings'] as const,
    listsByUser:    (id: number) => ['users', id, 'lists'] as const,
    notifications: () => ['users', 'me', 'notifications'] as const,
    followRequests: () => ['users', 'me', 'follow-requests'] as const,
    suggestions:  () => ['users', 'suggestions'] as const,
    followers:   (id: number) => ['users', id, 'followers'] as const,
    following:   (id: number) => ['users', id, 'following'] as const,
    followStatus: (id: number) => ['users', id, 'follow-status'] as const,
    search:      (q: string) => ['users', 'search', q] as const,
    badges:      () => ['users', 'me', 'badges'] as const,
    badgesByUser: (id: number) => ['users', id, 'badges'] as const,
  },
  // Ratings de un contenido concreto (por contentId de BD)
  ratings: {
    byContent: (contentId: number) => ['ratings', contentId] as const,
    likeStatus: (ratingId: number) => ['ratings', ratingId, 'like-status'] as const,
    likesCount: (ratingId: number) => ['ratings', ratingId, 'likes'] as const,
  },
  // Listas
  lists: {
    mine:   () => ['lists', 'mine'] as const,
    public: () => ['lists', 'public'] as const,
    detail: (id: number) => ['lists', id] as const,
  },
  // Notificaciones
  notifications: {
    unreadCount: () => ['notifications', 'unread-count'] as const,
  },
  // Comentarios
  comments: {
    byRating: (ratingId: number) => ['comments', ratingId] as const,
    byList:   (listId: number)   => ['comments', 'list', listId] as const,
  },
  // Feed de actividad
  feed: {
    personal: (page: number) => ['feed', 'personal', page] as const,
    global:   (page: number) => ['feed', 'global', page] as const,
  },
} as const