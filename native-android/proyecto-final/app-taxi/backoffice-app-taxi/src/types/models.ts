export type UserStatus = 'ACTIVE' | 'INACTIVE'

export type TripStatus = 'REQUESTED' | 'ACCEPTED' | 'ARRIVED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export type OfferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface GeoPoint {
  lat: number
  lng: number
  address?: string | null
}

export interface Vehicle {
  plate: string
  brand: string
  model: string
  color: string
}

export interface AdminDto {
  id: string
  fullName: string
  email: string
  status: UserStatus
  lastLoginAt: string | null
  createdAt: string
}

export interface AdminLoginResponse {
  accessToken: string
  admin: AdminDto
}

export interface AdminCreateRequest {
  fullName: string
  email: string
  password: string
}

export interface AdminUpdateRequest {
  fullName: string
  email: string
  password?: string
  status: UserStatus
}

export interface PassengerDto {
  id: string
  phone: string
  givenName: string | null
  familyName: string | null
  email: string | null
  photoUrl: string | null
  status: UserStatus
  lastLoginAt: string | null
  createdAt: string
}

export interface DriverDto {
  id: string
  phone: string
  givenName: string
  familyName: string
  email: string | null
  photoUrl: string | null
  status: UserStatus
  rating: number | null
  ratingCount: number
  vehicle: Vehicle
  createdAt: string
}

export interface AdminDriverDto extends DriverDto {
  online: boolean
}

export interface DriverCreateRequest {
  phone: string
  givenName: string
  familyName: string
  email?: string
  password: string
  vehicle: Vehicle
}

export interface DriverUpdateRequest {
  phone: string
  givenName: string
  familyName: string
  email?: string | null
  password?: string
  status: UserStatus
  vehicle: Vehicle
}

export interface DriverLocation {
  driverId: string
  tripId: string | null
  lat: number
  lng: number
  bearing: number | null
  speed: number | null
  at: string
}

export interface AdminDriverLocation extends DriverLocation {
  givenName: string
  familyName: string
  plate: string
  online: boolean
}

export interface DriverAvailabilityEvent {
  driverId: string
  online: boolean
}

export interface TripDriver {
  id: string
  givenName: string
  familyName: string
  phone: string
  photoUrl: string | null
  rating: number | null
  vehicle: Vehicle
}

export interface TripPassenger {
  id: string
  givenName: string | null
  familyName: string | null
  phone: string
  photoUrl: string | null
}

export interface TripDto {
  id: string
  status: TripStatus
  origin: GeoPoint
  destination: GeoPoint
  distanceMeters: number
  durationSeconds: number
  polyline: string | null
  estimatedFare: number
  finalFare: number | null
  currency: string
  passenger: TripPassenger
  driver: TripDriver | null
  ratingByPassenger: number | null
  ratingByDriver: number | null
  requestedAt: string
  acceptedAt: string | null
  arrivedAt: string | null
  startedAt: string | null
  completedAt: string | null
  cancelledAt: string | null
  cancelledBy: 'PASSENGER' | 'DRIVER' | null
}

export interface OfferDto {
  id: string
  tripId: string
  price: number
  currency: string
  etaSeconds: number | null
  distanceToPickupMeters: number | null
  status: OfferStatus
  createdAt: string
  driver: TripDriver
}

export interface TrackPoint {
  lat: number
  lng: number
  at: string
}

export interface TripDetailDto extends TripDto {
  offers: OfferDto[]
  track: TrackPoint[]
}

export interface TariffDto {
  baseFare: number
  pricePerKm: number
  currency: string
  updatedAt: string | null
}

export interface TariffUpdateRequest {
  baseFare: number
  pricePerKm: number
}

export interface Page<T> {
  items: T[]
  total: number
  page: number
  size: number
}

export interface PageQuery {
  page: number
  size: number
  search?: string
  status?: string
}

export interface ApiErrorBody {
  status_code?: number
  message?: string | string[]
  errors?: { field: string; message: string }[]
}

export const ACTIVE_TRIP_STATUSES: TripStatus[] = ['REQUESTED', 'ACCEPTED', 'ARRIVED', 'IN_PROGRESS']

export const ALL_TRIP_STATUSES: TripStatus[] = [
  'REQUESTED',
  'ACCEPTED',
  'ARRIVED',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED'
]
