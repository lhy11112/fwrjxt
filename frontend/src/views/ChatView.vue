<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { sendChatMessage } from '@/api'

const chat = useChatStore()
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

async function handleSend() {
  const text = inputMessage.value.trim()
  if (!text) return

  // 添加用户消息
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

    // 如果有可执行动作
    if (response.actions?.length) {
      chat.addMessage({
        message_id: crypto.randomUUID(),
        sender: 'system',
        content: `已执行操作: ${response.actions.map((a: any) => a.action_type).join(', ')}`,
        timestamp: new Date().toISOString(),
        message_type: 'SYSTEM',
      })
    }
  } catch (e) {
    chat.addMessage({
      message_id: crypto.randomUUID(),
      sender: 'system',
      content: '指令执行失败，请重试。',
      timestamp: new Date().toISOString(),
      message_type: 'SYSTEM',
    })
  } finally {
    chat.setProcessing(false)
  }

  // 滚动到底部
  setTimeout(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 100)
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 快捷命令
const quickCommands = [
  { label: '显示所有红色目标', command: '显示所有红色威胁目标' },
  { label: '自动拦截', command: '对所有高危目标自动生成拦截方案' },
  { label: '系统状态', command: '查看系统运行状态' },
  { label: '传感器概览', command: '显示所有在线传感器' },
]
</script>

<template>
  <div class="chat-view">
    <div class="chat-container panel">
      <!-- 对话历史 -->
      <div class="messages-area" ref="messagesContainer">
        <div v-if="chat.messages.length === 0" class="welcome-message">
          <div class="welcome-icon"><Icon name="chat" :size="40" /></div>
          <h3>HSimC2 智能指挥助手</h3>
          <p>您可以通过自然语言进行指挥控制操作</p>
          <p class="examples">例如：</p>
          <div class="quick-commands">
            <button v-for="cmd in quickCommands" :key="cmd.label"
                    class="btn quick-btn"
                    @click="inputMessage = cmd.command">
              {{ cmd.label }}
            </button>
          </div>
        </div>

        <div v-for="msg in chat.messages" :key="msg.message_id"
             class="message" :class="msg.message_type.toLowerCase()">
          <div class="message-avatar">
            <Icon :name="msg.message_type === 'USER' ? 'user' : msg.message_type === 'ASSISTANT' ? 'robot' : 'settings'" :size="22" />
          </div>
          <div class="message-content">
            <div class="message-sender">{{ msg.sender === 'user' ? '指挥员' : msg.sender === 'hsimc2' ? 'HSimC2 助手' : '系统' }}</div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
          <div class="message-time">{{ new Date(msg.timestamp).toLocaleTimeString() }}</div>
        </div>

        <div v-if="chat.isProcessing" class="message assistant">
          <div class="message-avatar"><Icon name="robot" :size="22" /></div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <textarea
          v-model="inputMessage"
          class="chat-input"
          placeholder="输入指令，例如：显示所有红色目标..."
          @keydown="handleKeydown"
          :disabled="chat.isProcessing"
          rows="2"
        ></textarea>
        <button class="btn btn-primary send-btn" @click="handleSend" :disabled="chat.isProcessing || !inputMessage.trim()">
          {{ chat.isProcessing ? '处理中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-view {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: var(--bg-primary);
}

.chat-container {
  width: 100%;
  max-width: 800px;
  height: 100%;
  max-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h3 {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.welcome-message p {
  color: var(--text-secondary);
  font-size: 14px;
}

.examples {
  margin-top: 24px;
  margin-bottom: 12px;
  font-weight: 600;
}

.quick-commands {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.quick-btn {
  font-size: 12px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-avatar {
  font-size: 24px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
}

.message-sender {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.message-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.system .message-text {
  color: var(--accent-yellow);
  font-size: 13px;
  font-style: italic;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-blue);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

.input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
}

.chat-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  resize: none;
  font-family: inherit;
}

.chat-input:focus {
  border-color: var(--accent-blue);
}

.chat-input:disabled {
  opacity: 0.6;
}

.send-btn {
  padding: 10px 24px;
  align-self: flex-end;
}
</style>
