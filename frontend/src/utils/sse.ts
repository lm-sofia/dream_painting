import type { GenerationTask } from '../types';

/**
 * SSE 订阅工具（第 5 课）。
 *
 * 为什么不用 EventSource API：浏览器 EventSource 无法自定义请求头，
 * 而本项目 JWT 走 `Authorization: Bearer` → 用 fetch（支持 header）
 * + ReadableStream 手解 SSE 线协议。
 *
 * SSE 线协议格式（服务端 SseEmitter 输出）：
 *   event:progress\n
 *   data:{"id":1,...}\n
 *   \n                ← 空行 = 帧结束
 *
 * 生产进阶：改用 HttpOnly Cookie 认证后可直接用 EventSource（更省心）。
 *
 * @param url    订阅地址（含 taskId）
 * @param token  JWT
 * @param onData 每收到一帧 progress 回调
 * @param onDone 流正常结束（服务端 complete，如终态）
 * @param onError 连接/流异常（调用方可降级轮询兜底）
 * @returns 取消函数（组件卸载时调用，防止泄漏）
 */
export function subscribeSSE(
  url: string,
  token: string,
  handlers: {
    onData: (task: GenerationTask) => void;
    onDone?: () => void;
    onError?: () => void;
  },
): () => void {
  let cancelled = false;
  const controller = new AbortController();
  const { onData, onDone, onError } = handlers;

  (async () => {
    try {
      const resp = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal,
      });
      if (!resp.ok || !resp.body) {
        throw new Error(`SSE 连接失败: HTTP ${resp.status}`);
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (!cancelled) {
        const { done, value } = await reader.read();
        if (done) {
          onDone?.();
          break;
        }

        // 解码增量字节，按空行（\n\n）切帧；最后一段可能不完整，留到下一轮
        buffer += decoder.decode(value, { stream: true });
        const frames = buffer.split('\n\n');
        buffer = frames.pop() ?? '';

        for (const frame of frames) {
          const dataLine = frame.split('\n').find((line) => line.startsWith('data:'));
          if (!dataLine) continue;
          try {
            onData(JSON.parse(dataLine.slice(5).trim()) as GenerationTask);
          } catch {
            // 单帧解析失败不影响后续帧
          }
        }
      }
    } catch (err) {
      // 主动取消（AbortError）静默；其他异常交给调用方降级
      if (!cancelled && !(err instanceof DOMException && err.name === 'AbortError')) {
        onError?.();
      }
    }
  })();

  return () => {
    cancelled = true;
    controller.abort();
  };
}
