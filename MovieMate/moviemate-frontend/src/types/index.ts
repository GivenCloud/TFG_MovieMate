export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number          // página actual (0-indexed)
  size: number
  first: boolean
  last: boolean
}

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

export type ContentType = 'MOVIE' | 'TV'

export interface AddToListRequest {
    tmdbId: number
    contentType: ContentType
}

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

export interface ErrorResponse {
    error: string
    message: string
    status: number
    timestamp: string
    details: { [key: string]: string }
}

export type FollowRequestStatus = 'ACCEPTED' | 'REJECTED'

export interface FollowRequestActionResponse {
    requestId: number
    senderId: number
    receiverId: number
    status: FollowRequestStatus
    actionAt: string
}

export interface FollowRequestDto {
    id: number
    senderId: number
    senderUsername: string
    senderAvatarUrl: string
    createdAt: string
}

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

export interface LoginRequest {
    usernameOrEmail: string
    password: string
}

export type NotificationType = 'FOLLOW_REQUEST' | 'FOLLOW_REQUEST_ACCEPTED' | 'FOLLOWER' | 'REVIEW_LIKE'


export interface NotificationDto {
    id: number
    type: NotificationType
    referenceId: number
    read: boolean
    createdAt: string
    senderId: number
    senderUsername?: string
    senderAvatarUrl?: string
}

export type EmotionalTag = 'INCREIBLE' | 'RECOMENDADA' | 'ENTRETENIDA' | 'REGULAR' | 'DECEPCIONANTE'
export type Status = 'POR_VER' | 'EN_PROGRESO' | 'VISTA' | 'ABANDONADA' | 'PAUSADA'

export interface RatingRequest {
    tmdbId: number
    contentType: ContentType
    rating: number
    reviewText?: string
    emotionaltag: EmotionalTag
    status: Status
    wathchedDate: string
}

export interface RatingResponse {
    id: number
    rating: number
    reviewText?: string
    emotionaltag: EmotionalTag
    status: Status
    wathchedDate: string
    createdAt: string
    user: UserResponse
    content: ContentResponse
}

export interface RegisterRequest {
    username: string
    email: string
    password: string
}

export interface UpdateProfileRequest {
    bio?: string
    avatarUrl?: string
}

export interface UpdateProfilePublicStatusRequest {
    isPrivate: boolean
}

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

export interface UserResponse {
    id: number
    username: string
    email: string
    avatarUrl?: string
    bio?: string
    isPublic: boolean
    createdAt: string
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