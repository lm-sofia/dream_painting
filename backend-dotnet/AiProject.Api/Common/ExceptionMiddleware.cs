using System.Text.Json;

namespace AiProject.Api.Common;

/// <summary>
/// 全局异常中间件：统一转换为 {code, message, data} 信封，禁止泄露堆栈（安全基线）。
/// </summary>
public class ExceptionMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionMiddleware> _logger;

    public ExceptionMiddleware(RequestDelegate next, ILogger<ExceptionMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (BizException ex)
        {
            context.Response.StatusCode = ex.Code switch
            {
                ErrorCodes.BadRequest => StatusCodes.Status400BadRequest,
                ErrorCodes.Unauthorized => StatusCodes.Status401Unauthorized,
                ErrorCodes.Forbidden => StatusCodes.Status403Forbidden,
                ErrorCodes.NotFound => StatusCodes.Status404NotFound,
                ErrorCodes.Conflict => StatusCodes.Status409Conflict,
                _ => StatusCodes.Status500InternalServerError
            };
            await WriteJsonAsync(context, new { code = ex.Code, message = ex.Message, data = (object?)null });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unhandled exception");
            context.Response.StatusCode = StatusCodes.Status500InternalServerError;
            await WriteJsonAsync(context, new { code = ErrorCodes.InternalError, message = "服务器内部错误", data = (object?)null });
        }
    }

    private static async Task WriteJsonAsync(HttpContext context, object payload)
    {
        context.Response.ContentType = "application/json; charset=utf-8";
        await context.Response.WriteAsync(JsonSerializer.Serialize(payload));
    }
}
