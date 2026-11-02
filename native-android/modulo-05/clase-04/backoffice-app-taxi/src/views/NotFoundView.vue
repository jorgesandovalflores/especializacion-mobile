<template>
  <div class="not-found">
    <p class="code">404</p>
    <h1>Página no encontrada</h1>
    <p class="muted">La dirección que buscas no existe o fue movida.</p>
    <RouterLink to="/" class="btn btn-primary">Volver al inicio</RouterLink>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  text-align: center;
}

.code {
  margin: 0;
  font-size: 72px;
  font-weight: 900;
  color: var(--color-primary);
}

.btn {
  text-decoration: none;
}
</style>
