namespace AiProject.Api.Common;

/// <summary>
/// 业务错误码，与 database/API_CONTRACT.md 第 2 节错误码表一一对应。
/// </summary>
public static class ErrorCodes
{
    public const int BadRequest = 40001;
    public const int Unauthorized = 40101;
    public const int Forbidden = 40301;
    public const int NotFound = 40401;
    public const int Conflict = 40901;
    public const int InternalError = 50000;
}
