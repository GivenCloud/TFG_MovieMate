// ─── Paginación ────────────────────────────────────────────────
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number          // página actual (0-indexed)
  size: number
  first: boolean
  last: boolean
}

// ─── Auth ──────────────────────────────────────────────────────
export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  email: string
  message: string
}

// ─── Contenido ─────────────────────────────────────────────────
export type ContentType = 'MOVIE' | 'TV'

export interface ContentResponse {
  id: number
  tmdbId: number
  contentType: ContentType
  title: string
  releaseDate: string
  synopsis: string
  posterUrl: string
  backdropUrl: string
  genres: string[]
  tmdbRating: number
  tmdbVoteCount: number
  appRating: number
  appVoteCount: number
}

// ─── Usuario ───────────────────────────────────────────────────
// Versión compacta — embebida en ratings, listas, notificaciones, etc.
export interface UserResponse {
  id: number
  username: string
  email: string
  avatarUrl?: string
  bio?: string
  isPublic: boolean
  role?: 'USER' | 'ADMIN'
  banned?: boolean
  createdAt: string
}

// Versión completa — página de perfil
export interface UserProfileResponse {
  id: number
  username: string
  email: string
  avatarUrl?: string
  bio?: string
  isPublic: boolean
  createdAt: string
  followersCount: number
  followingCount: number
  isFollowing: boolean
}

export interface UserStatsResponse {
  totalRatings: number
  averageRating: number
  moviesWatched: number
  seriesWatched: number
  totalWatchTime: number
  listsCreated: number
  followersCount: number
  followingCount: number
  likesReceived: number
}

export interface UpdateProfileRequest {
  bio?: string
  avatarUrl?: string
}

export interface UpdateProfilePublicStatusRequest {
  isPrivate: boolean
}

// ─── Valoraciones ──────────────────────────────────────────────
export type EmotionalTag =
  | 'INCREIBLE'
  | 'RECOMENDADA'
  | 'ENTRETENIDA'
  | 'REGULAR'
  | 'DECEPCIONANTE'

export type Status =
  | 'POR_VER'
  | 'EN_PROGRESO'
  | 'VISTA'
  | 'ABANDONADA'
  | 'PAUSADA'

export interface RatingRequest {
  tmdbId: number
  contentType: ContentType
  rating: number
  reviewText?: string
  emotionalTag: EmotionalTag
  status: Status
  watchedDate: string
  containsSpoiler?: boolean
}

export interface RatingResponse {
  id: number
  rating: number
  reviewText?: string
  emotionalTag: EmotionalTag
  status: Status
  watchedDate: string
  createdAt: string
  user: UserResponse
  content: ContentResponse
  likesCount?: number
  likedByCurrentUser?: boolean
  containsSpoiler?: boolean
}

// ─── Listas ────────────────────────────────────────────────────
export type ListType = 'CUSTOM' | 'FAVORITES' | 'WATCHLIST' | 'WATCHED'

export interface ListRequest {
  name: string
  description?: string
  isPublic: boolean
  listType: ListType
}

export interface ListResponse {
  id: number
  name: string
  description?: string
  isPublic: boolean
  listType: ListType
  itemCount: number
  createdAt: string
  user: UserResponse
  contents: ContentResponse[]
}

export interface AddToListRequest {
  tmdbId: number
}

// ─── Seguimiento ───────────────────────────────────────────────
export type FollowRequestStatus = 'ACCEPTED' | 'REJECTED'

export interface FollowRequestDto {
  id: number
  senderId: number
  senderUsername: string
  senderAvatarUrl: string
  createdAt: string
}

export interface FollowRequestActionResponse {
  requestId: number
  senderId: number
  receiverId: number
  status: FollowRequestStatus
  actionAt: string
}

// ─── Notificaciones ────────────────────────────────────────────
export type NotificationType =
  | 'FOLLOW_REQUEST'
  | 'FOLLOW_REQUEST_ACCEPTED'
  | 'FOLLOWER'
  | 'REVIEW_LIKE'
  | 'COMMENT_ON_RATING'
  | 'CONTENT_REMOVED'

export interface NotificationDto {
  id: number
  type: NotificationType
  referenceId: number
  read: boolean
  createdAt: string
  senderId: number
  senderUsername?: string
  senderAvatarUrl?: string
  message?: string
}

// ─── Feed de actividad ─────────────────────────────────────────
export type ActivityType =
  | 'RATING_CREATED'
  | 'RATING_UPDATED'
  | 'LIST_CREATED'
  | 'LIST_UPDATED'
  | 'FOLLOW'
  | 'COMMENT_ADDED_TO_LIST'

export interface ActivityResponse {
  type: ActivityType
  user: UserResponse
  createdAt: string
  rating?: RatingResponse
  list?: ListResponse
  targetUser?: UserResponse
  content?: ContentResponse
}

// ─── Reportes ──────────────────────────────────────────────────
export interface ReportResponse {
  id: number
  targetType: 'RATING' | 'COMMENT'
  targetId: number
  reason: 'SPAM' | 'INAPPROPRIATE' | 'SPOILER' | 'OTHER'
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED'
  createdAt: string
  reporter: UserResponse
}

// ─── Comentarios ───────────────────────────────────────────────
export interface CommentResponse {
  id: number
  content: string
  createdAt: string
  updatedAt: string
  author: UserResponse
  ratingId: number
}

export interface CommentRequest {
  content: string
}

// ─── Personas ──────────────────────────────────────────────────
export interface PersonDto {
  id: number
  name: string
  biography?: string
  birthday?: string
  deathday?: string
  profileUrl?: string
  placeOfBirth?: string
  knownForDepartment?: string
}

export interface CastMemberDto {
  personId: number
  name: string
  profileUrl?: string
  character?: string
  job?: string
  department?: string
}

// ─── Estadísticas avanzadas ──────────────────────────────────────
export interface RatingCountDto {
  rating: number
  count: number
}

export interface GenreStatDto {
  genre: string
  count: number
}

export interface MonthlyActivityDto {
  year: number
  month: number
  count: number
}

export interface FullStatsDto {
  totalRatings: number
  averageRating: number
  moviesWatched: number
  seriesWatched: number
  totalWatchTime: number
  listsCreated: number
  followersCount: number
  followingCount: number
  likesReceived: number
  ratingDistribution: RatingCountDto[]
  topGenres: GenreStatDto[]
  monthlyActivity: MonthlyActivityDto[]
}

// ─── Temporadas y episodios ─────────────────────────────────────
export interface SeasonSummary {
  seasonNumber: number
  name: string
  overview: string
  episodeCount: number
  posterUrl?: string
  airDate?: string
}

export interface EpisodeDto {
  episodeNumber: number
  name: string
  overview: string
  airDate?: string
  runtime?: number
  stillUrl?: string
  voteAverage?: number
}

export interface SeasonDto extends SeasonSummary {
  episodes: EpisodeDto[]
}

// ─── Errores ───────────────────────────────────────────────────
export interface ErrorResponse {
  error: string
  message: string
  status: number
  timestamp: string
  details: { [key: string]: string }
}

// ─── Insignias ────────────────────────────────────────────────
export interface BadgeDto {
  type: string
  name: string
  description: string
  icon: string
  awardedAt: string
}

// ─── Progreso de episodios ────────────────────────────────────
export interface SeriesProgressDto {
  tmdbSeriesId: number
  title: string
  posterUrl: string | null
  watchedCount: number
}

// ─── Comentarios de listas ────────────────────────────────────
export interface ListCommentResponse {
  id: number
  content: string
  createdAt: string
  updatedAt: string
  author: UserResponse
  listId: number
}
