import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const isProcessing = ref(false)

  function addMessage(msg: ChatMessage) {
    messages.value.push(msg)
    // 只保留最近 200 条消息
    if (messages.value.length > 200) {
      messages.value = messages.value.slice(-200)
    }
  }

  function setProcessing(val: boolean) {
    isProcessing.value = val
  }

  function clearMessages() {
    messages.value = []
  }

  return {
    messages,
    isProcessing,
    addMessage,
    setProcessing,
    clearMessages,
  }
})
