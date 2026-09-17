using AiProject.Api.Common;
using AiProject.Api.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace AiProject.Api.Controllers;

[ApiController]
[Route("api/v1/health")]
public class HealthController : ControllerBase
{
    private readonly AppDbContext _db;

    public HealthController(AppDbContext db)
    {
        _db = db;
    }

    [HttpGet]
    [AllowAnonymous]
    public async Task<ActionResult<ApiResponse<object>>> Health()
    {
        string dbStatus = "UP";
        try
        {
            await _db.Database.ExecuteSqlRawAsync("SELECT 1");
        }
        catch
        {
            dbStatus = "DOWN";
        }

        return ApiResponse<object>.Ok(new
        {
            status = "UP",
            service = "backend-dotnet",
            db = dbStatus,
            timestamp = DateTime.UtcNow.ToString("o")
        });
    }
}
