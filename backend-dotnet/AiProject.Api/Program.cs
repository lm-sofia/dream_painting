using System.Text;
using AiProject.Api.Common;
using AiProject.Api.Data;
using AiProject.Api.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

// ---------- 数据库（PostgreSQL，测试用 InMemory） ----------
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
if (builder.Environment.IsEnvironment("Test"))
{
    builder.Services.AddDbContext<AppDbContext>(opt =>
        opt.UseInMemoryDatabase("testdb"));
}
else
{
    builder.Services.AddDbContext<AppDbContext>(opt =>
        opt.UseNpgsql(connectionString));
}

// ---------- 认证：JWT Bearer ----------
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        // 密钥延迟求值：确保 WebApplicationFactory 注入的测试配置已生效
        var jwtSecret = builder.Configuration["Jwt:Secret"]
            ?? throw new InvalidOperationException("Jwt:Secret 未配置");
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = builder.Configuration["Jwt:Issuer"],
            ValidateAudience = true,
            ValidAudience = builder.Configuration["Jwt:Audience"],
            ValidateLifetime = true,
            ClockSkew = TimeSpan.Zero,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret))
        };
    });
builder.Services.AddAuthorization();

// ---------- 业务服务 ----------
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<ArticleService>();
builder.Services.AddSingleton<JwtTokenService>();

// ---------- API 行为 ----------
builder.Services.AddControllers()
    .ConfigureApiBehaviorOptions(options =>
        // 禁用 [ApiController] 自动 400 响应，统一走我们的信封格式
        options.SuppressModelStateInvalidFilter = true);
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// ---------- CORS（白名单，安全基线） ----------
var corsOrigin = builder.Configuration["Cors:Origin"] ?? "http://localhost:5173";
builder.Services.AddCors(options =>
{
    options.AddPolicy("frontend", policy =>
        policy.WithOrigins(corsOrigin)
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials());
});

var app = builder.Build();

// ---------- 中间件管线 ----------
if (app.Environment.IsDevelopment() || app.Environment.IsEnvironment("Test"))
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseMiddleware<ExceptionMiddleware>();
app.UseCors("frontend");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();

/// <summary>供集成测试引用的入口。</summary>
public partial class Program { }
