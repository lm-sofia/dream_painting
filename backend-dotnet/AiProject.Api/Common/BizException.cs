namespace AiProject.Api.Common;

/// <summary>
/// 业务异常：携带错误码，由 ExceptionMiddleware 统一转换为响应信封。
/// </summary>
public class BizException : Exception
{
    public int Code { get; }

    public BizException(int code, string message) : base(message)
    {
        Code = code;
    }
}
