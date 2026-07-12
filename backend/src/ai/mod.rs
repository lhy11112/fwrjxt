/// DeepSeek AI 集成模块
use serde::{Deserialize, Serialize};

/// DeepSeek 配置
#[derive(Debug, Clone)]
pub struct DeepSeekConfig {
    pub api_key: String,
    pub base_url: String,
    pub model: String,
}

impl Default for DeepSeekConfig {
    fn default() -> Self {
        Self {
            api_key: std::env::var("DEEPSEEK_API_KEY")
                .unwrap_or_else(|_| "sk-d39cbd3e7cab447aae3c37327b5c1f57".into()),
            base_url: "https://api.deepseek.com".into(),
            model: std::env::var("DEEPSEEK_MODEL")
                .unwrap_or_else(|_| "deepseek-v4-flash".into()),
        }
    }
}

/// DeepSeek 聊天消息
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ChatMessage {
    pub role: String,  // "system" | "user" | "assistant"
    pub content: String,
}

/// DeepSeek 请求体
#[derive(Debug, Serialize)]
struct DeepSeekRequest {
    model: String,
    messages: Vec<ChatMessage>,
    stream: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    max_tokens: Option<u32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    temperature: Option<f32>,
}

/// DeepSeek 非流式响应
#[derive(Debug, Deserialize)]
struct DeepSeekResponse {
    choices: Vec<DeepSeekChoice>,
}

#[derive(Debug, Deserialize)]
struct DeepSeekChoice {
    message: DeepSeekMsg,
}

#[derive(Debug, Deserialize)]
struct DeepSeekMsg {
    content: String,
}

/// DeepSeek 流式响应 chunk
#[derive(Debug, Deserialize)]
struct DeepSeekStreamChunk {
    choices: Vec<DeepSeekStreamChoice>,
}

#[derive(Debug, Deserialize)]
struct DeepSeekStreamChoice {
    delta: DeepSeekStreamDelta,
    #[serde(default)]
    finish_reason: Option<String>,
}

#[derive(Debug, Deserialize)]
struct DeepSeekStreamDelta {
    #[serde(default)]
    content: String,
}

/// DeepSeek 客户端
pub struct DeepSeekClient {
    config: DeepSeekConfig,
    client: reqwest::Client,
}

impl DeepSeekClient {
    pub fn new(config: DeepSeekConfig) -> Self {
        Self {
            config,
            client: reqwest::Client::new(),
        }
    }

    /// 构建系统提示词（反无人机指控平台场景）
    fn system_prompt() -> String {
        r#"你是一个反无人机一体化指挥控制平台(HSimC2)的智能助手。
你可以帮助操作员完成以下任务：
1. 分析当前空中态势，识别高危无人机目标
2. 建议拦截策略和反制手段（干扰、诱骗、动能拦截等）
3. 解释传感器数据、威胁评估结果
4. 规划任务并分配拦截资源
5. 回答无人机防御相关问题

请使用专业、简洁的军事化语言回复。回复时可以直接指导操作员执行具体的指控操作。"#.into()
    }

    /// 非流式聊天
    pub async fn chat(&self, user_message: &str, history: &[(String, String)]) -> anyhow::Result<String> {
        let mut messages = vec![
            ChatMessage { role: "system".into(), content: Self::system_prompt() },
        ];
        for (user, assistant) in history {
            messages.push(ChatMessage { role: "user".into(), content: user.clone() });
            messages.push(ChatMessage { role: "assistant".into(), content: assistant.clone() });
        }
        messages.push(ChatMessage { role: "user".into(), content: user_message.to_string() });

        let req_body = DeepSeekRequest {
            model: self.config.model.clone(),
            messages,
            stream: false,
            max_tokens: Some(2048),
            temperature: Some(0.7),
        };

        let resp = self.client
            .post(format!("{}/chat/completions", self.config.base_url))
            .header("Authorization", format!("Bearer {}", self.config.api_key))
            .header("Content-Type", "application/json")
            .json(&req_body)
            .send()
            .await?;

        if !resp.status().is_success() {
            let status = resp.status();
            let body = resp.text().await.unwrap_or_default();
            anyhow::bail!("DeepSeek API error {}: {}", status, body);
        }

        let data: DeepSeekResponse = resp.json().await?;
        let reply = data.choices
            .first()
            .map(|c| c.message.content.clone())
            .unwrap_or_else(|| "（模型返回为空）".into());

        Ok(reply)
    }

    /// 流式聊天（返回 SSE body 字符串，适用于直接转发给前端）
    pub async fn chat_stream(
        &self,
        user_message: &str,
        history: &[(String, String)],
    ) -> anyhow::Result<String> {
        let mut messages = vec![
            ChatMessage { role: "system".into(), content: Self::system_prompt() },
        ];
        for (user, assistant) in history {
            messages.push(ChatMessage { role: "user".into(), content: user.clone() });
            messages.push(ChatMessage { role: "assistant".into(), content: assistant.clone() });
        }
        messages.push(ChatMessage { role: "user".into(), content: user_message.to_string() });

        let req_body = DeepSeekRequest {
            model: self.config.model.clone(),
            messages,
            stream: true,
            max_tokens: Some(2048),
            temperature: Some(0.7),
        };

        let resp = self.client
            .post(format!("{}/chat/completions", self.config.base_url))
            .header("Authorization", format!("Bearer {}", self.config.api_key))
            .header("Content-Type", "application/json")
            .json(&req_body)
            .send()
            .await?;

        if !resp.status().is_success() {
            let status = resp.status();
            let body = resp.text().await.unwrap_or_default();
            anyhow::bail!("DeepSeek API error {}: {}", status, body);
        }

        // 收集所有 chunks，拼接完整回复
        let full_body = resp.text().await?;
        let mut full_reply = String::new();

        for line in full_body.lines() {
            let line = line.trim();
            if line.is_empty() || !line.starts_with("data: ") {
                continue;
            }
            let data = &line[6..]; // skip "data: "
            if data == "[DONE]" {
                break;
            }
            if let Ok(chunk) = serde_json::from_str::<DeepSeekStreamChunk>(data) {
                for choice in &chunk.choices {
                    full_reply.push_str(&choice.delta.content);
                }
            }
        }

        Ok(full_reply)
    }
}
