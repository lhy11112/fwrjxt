/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'nprogress' {
  const nprogress: {
    start: () => void
    done: () => void
    configure: (opts: any) => void
  }
  export default nprogress
}
