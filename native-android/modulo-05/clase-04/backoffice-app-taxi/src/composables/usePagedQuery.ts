import { onBeforeUnmount, ref, shallowRef, watch, type Ref } from 'vue'
import { apiErrorMessage } from '@/api/http'
import type { Page, PageQuery } from '@/types/models'

export function usePagedQuery<T>(
  fetcher: (query: PageQuery) => Promise<Page<T>>,
  options: { size?: number; errorMessage: string; filter?: Ref<string> }
) {
  const items = shallowRef<T[]>([])
  const total = ref(0)
  const page = ref(1)
  const size = options.size ?? 20
  const search = ref('')
  const loading = ref(true)
  const error = ref<string | null>(null)
  let requestId = 0
  let debounce: number | undefined

  async function load() {
    const current = ++requestId
    loading.value = true
    error.value = null
    try {
      const result = await fetcher({
        page: page.value,
        size,
        search: search.value.trim() || undefined,
        status: options.filter?.value || undefined
      })
      if (current !== requestId) return
      items.value = result.items
      total.value = result.total
      const lastPage = Math.max(1, Math.ceil(result.total / size))
      if (page.value > lastPage && result.total > 0) {
        page.value = lastPage
        return load()
      }
    } catch (err) {
      if (current !== requestId) return
      error.value = apiErrorMessage(err, options.errorMessage)
    } finally {
      if (current === requestId) loading.value = false
    }
  }

  function goTo(next: number) {
    page.value = next
    load()
  }

  watch(search, () => {
    window.clearTimeout(debounce)
    debounce = window.setTimeout(() => {
      page.value = 1
      load()
    }, 350)
  })

  if (options.filter) {
    watch(options.filter, () => {
      page.value = 1
      load()
    })
  }

  onBeforeUnmount(() => window.clearTimeout(debounce))

  return { items, total, page, size, search, loading, error, load, goTo }
}
