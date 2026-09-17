namespace AiProject.Api.Common;

/// <summary>
/// 统一响应信封，对应 API_CONTRACT.md 第 2 节。
/// </summary>
public class ApiResponse<T>
{
    public int Code { get; set; }
    public string Message { get; set; } = "ok";
    public T? Data { get; set; }

    public static ApiResponse<T> Ok(T data) => new() { Code = 0, Message = "ok", Data = data };

    public static ApiResponse<object?> Ok() => new() { Code = 0, Message = "ok", Data = null };

    public static ApiResponse<T> Error(int code, string message) => new() { Code = code, Message = message };
}
