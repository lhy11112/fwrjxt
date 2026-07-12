<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import { sendChatMessage } from '@/api'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

async function handleSend() {
  const text = inputMessage.value.trim()
  if (!text) return

  chat.addMessage({
    message_id: crypto.randomUUID(),
    sender: 'user',
    content: text,
    timestamp: new Date().toISOString(),
    message_type: 'USER',
  })

  inputMessage.value = ''
  chat.setProcessing(true)

  try {
    const response = await sendChatMessage(text)
    chat.addMessage({
      message_id: crypto.randomUUID(),
      sender: 'hsimc2',
      content: response.reply,
      timestamp: new Date().toISOString(),
      message_type: 'ASSISTANT',
    })
    if (response.actions?.length) {
      chat.addMessage({
        message_id: crypto.randomUUID(),
        sender: 'system',
        content: `已执行: ${response.actions.map((a: any) => a.action_type).join(', ')}`,
        timestamp: new Date().toISOString(),
        message_type: 'SYSTEM',
      })
    }
  } catch {
    chat.addMessage({
      message_id: crypto.randomUUID(),
      sender: 'system',
      content: '指令执行失败，请重试',
      timestamp: new Date().toISOString(),
      message_type: 'SYSTEM',
    })
  } finally {
    chat.setProcessing(false)
  }

  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const quickCommands = [
  '显示所有红色威胁目标',
  '对所有高危目标生成拦截方案',
  '查看系统运行状态',
  '显示所有在线传感器',
]
</script>

<template>
  <div class="chat-panel">
    <!-- 消息列表 -->
    <div ref="messagesContainer" class="cp-messages">
      <div v-if="chat.messages.length === 0" class="cp-welcome">
        <div class="cp-welcome-icon"><Icon name="chat" :size="26" /></div>
        <p>智能指挥助手</p>
        <div class="cp-quick">
          <button v-for="cmd in quickCommands" :key="cmd"
                  class="cp-quick-btn"
                  @click="inputMessage = cmd; handleSend()">
            {{ cmd.length > 12 ? cmd.slice(0,12) + '...' : cmd }}
          </button>
        </div>
      </div>

      <div v-for="msg in chat.messages" :key="msg.message_id"
           class="cp-msg" :class="msg.message_type.toLowerCase()">
        <span class="cp-msg-avatar">
          <Icon :name="msg.message_type === 'USER' ? 'user' : msg.message_type === 'ASSISTANT' ? 'robot' : 'settings'" :size="15" />
        </span>
        <div class="cp-msg-body">
          <div class="cp-msg-text">{{ msg.content }}</div>
        </div>
      </div>

      <div v-if="chat.isProcessing" class="cp-msg assistant">
        <span class="cp-msg-avatar"><Icon name="robot" :size="15" /></span>
        <div class="cp-msg-body">
          <div class="cp-typing"><span></span><span></span><span></span></div>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="cp-input-area">
      <input
        v-model="inputMessage"
        class="cp-input"
        placeholder="输入指令..."
        @keydown="handleKeydown"
        :disabled="chat.isProcessing"
      />
      <button class="cp-send" @click="handleSend"
              :disabled="chat.isProcessing || !inputMessage.trim()">
        ➤
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

/* ---- 消息区 ---- */
.cp-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px 10px;
  min-height: 0;
}
.cp-messages::-webkit-scrollbar { width: 8px; }
.cp-messages::-webkit-scrollbar-thumb { background: var(--stroke-control); border-radius: var(--radius-circular); border: 2px solid transparent; background-clip: padding-box; }

.cp-welcome {
  text-align: center;
  padding: 24px 4px;
}
.cp-welcome-icon { font-size: 28px; margin-bottom: 6px; }
.cp-welcome p { font-size: 12px; color: var(--fg-3); margin: 0 0 10px; }

.cp-quick { display: flex; flex-direction: column; gap: var(--sp-xs); }
.cp-quick-btn {
  width: 100%;
  padding: 8px 10px;
  background: var(--bg-surface-2);
  border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-md);
  color: var(--fg-2);
  font-family: var(--font-base);
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: background var(--dur-normal) var(--ease-fluent), border-color var(--dur-normal) var(--ease-fluent);
}
.cp-quick-btn:hover {
  background: var(--bg-subtle-hover);
  border-color: var(--stroke-control);
  color: var(--fg-1);
}

/* ---- 单条消息 ---- */
.cp-msg {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  animation: cpFadeIn 0.25s ease;
}
@keyframes cpFadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}
.cp-msg-avatar { font-size: 16px; flex-shrink: 0; line-height: 1.4; }
.cp-msg-body { flex: 1; min-width: 0; }
.cp-msg-text {
  font-size: 12px;
  color: var(--fg-2);
  line-height: 1.5;
  word-break: break-word;
}
.system .cp-msg-text { color: var(--warning); font-style: italic; font-size: 11px; }

.cp-typing { display: flex; gap: 3px; padding: 4px 0; }
.cp-typing span {
  width: 5px; height: 5px; border-radius: var(--radius-circular);
  background: var(--brand-fg);
  animation: cpTyping 1.2s infinite;
}
.cp-typing span:nth-child(2) { animation-delay: 0.2s; }
.cp-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes cpTyping {
  0%,60%,100% { opacity: 0.3; }
  30% { opacity: 1; }
}

/* ---- 输入区 ---- */
.cp-input-area {
  display: flex;
  gap: var(--sp-s);
  padding: var(--sp-s) 10px;
  border-top: 1px solid var(--divider);
  flex-shrink: 0;
}
.cp-input {
  flex: 1;
  min-width: 0;
  height: 32px;
  padding: 0 var(--sp-m);
  background: var(--bg-surface);
  border: 1px solid var(--stroke-control);
  border-bottom-color: var(--fg-4);
  border-radius: var(--radius-md);
  color: var(--fg-1);
  font-size: 13px;
  outline: none;
  font-family: var(--font-base);
  transition: border-color var(--dur-normal) var(--ease-fluent);
}
.cp-input:hover { border-color: var(--fg-4); }
.cp-input:focus { border-color: var(--stroke-control); border-bottom: 2px solid var(--brand-fg); }
.cp-input::placeholder { color: var(--fg-4); }
.cp-input:disabled { opacity: 0.5; }
.cp-send {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: var(--brand-rest);
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 0 12px var(--glow-accent);
  transition: background var(--dur-normal) var(--ease-fluent), box-shadow var(--dur-normal) var(--ease-fluent);
}
.cp-send:hover:not(:disabled) { background: var(--brand-hover); box-shadow: 0 0 18px var(--glow-accent); }
.cp-send:disabled { opacity: 0.3; cursor: default; }
</style>
