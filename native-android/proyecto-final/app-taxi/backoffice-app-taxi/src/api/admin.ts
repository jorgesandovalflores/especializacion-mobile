import { http } from './http'
import type {
  AdminCreateRequest,
  AdminDriverDto,
  AdminDriverLocation,
  AdminDto,
  AdminLoginResponse,
  AdminUpdateRequest,
  DriverCreateRequest,
  DriverDto,
  DriverUpdateRequest,
  Page,
  PageQuery,
  PassengerDto,
  TariffDto,
  TariffUpdateRequest,
  TripDetailDto,
  TripDto
} from '@/types/models'

function cleanQuery(query: PageQuery) {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== '' && value !== undefined))
}

export const authApi = {
  login: (email: string, password: string) =>
    http.post<AdminLoginResponse>('/admin/auth/login', { email, password }).then((r) => r.data),
  me: () => http.get<AdminDto>('/admin/me').then((r) => r.data)
}

export const adminsApi = {
  list: () => http.get<AdminDto[]>('/admin/admins').then((r) => r.data),
  create: (body: AdminCreateRequest) => http.post<AdminDto>('/admin/admins', body).then((r) => r.data),
  update: (id: string, body: AdminUpdateRequest) => http.put<AdminDto>(`/admin/admins/${id}`, body).then((r) => r.data),
  remove: (id: string) => http.delete<void>(`/admin/admins/${id}`).then(() => undefined)
}

export const passengersApi = {
  list: (query: PageQuery) =>
    http.get<Page<PassengerDto>>('/admin/passengers', { params: cleanQuery(query) }).then((r) => r.data)
}

export const driversApi = {
  list: (query: PageQuery) =>
    http.get<Page<AdminDriverDto>>('/admin/drivers', { params: cleanQuery(query) }).then((r) => r.data),
  get: (id: string) => http.get<DriverDto>(`/admin/drivers/${id}`).then((r) => r.data),
  create: (body: DriverCreateRequest) => http.post<DriverDto>('/admin/drivers', body).then((r) => r.data),
  update: (id: string, body: DriverUpdateRequest) => http.put<DriverDto>(`/admin/drivers/${id}`, body).then((r) => r.data),
  remove: (id: string) => http.delete<void>(`/admin/drivers/${id}`).then(() => undefined),
  locations: () => http.get<AdminDriverLocation[]>('/admin/drivers/locations').then((r) => r.data)
}

export const tripsApi = {
  list: (query: PageQuery) => http.get<Page<TripDto>>('/admin/trips', { params: cleanQuery(query) }).then((r) => r.data),
  active: () => http.get<TripDto[]>('/admin/trips/active').then((r) => r.data),
  get: (id: string) => http.get<TripDetailDto>(`/admin/trips/${id}`).then((r) => r.data)
}

export const tariffApi = {
  get: () => http.get<TariffDto>('/admin/tariff').then((r) => r.data),
  update: (body: TariffUpdateRequest) => http.put<TariffDto>('/admin/tariff', body).then((r) => r.data)
}
