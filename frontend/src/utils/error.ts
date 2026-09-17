/** 业务错误码，与后端契约一致 */
export const ErrorCodes = {
  BadRequest: 40001,
  Unauthorized: 40101,
  Forbidden: 40301,
  NotFound: 40401,
  Conflict: 40901,
  InternalError: 50000,
} as const;

/** 前端业务异常 */
export class ApiError extends Error {
  code: number;
  status: number;

  constructor(code: number, message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}
