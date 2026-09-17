using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using Xunit;

namespace AiProject.Api.Tests;

public class AuthFlowTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public AuthFlowTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder =>
        {
            builder.UseSetting("Environment", "Test");
            builder.ConfigureAppConfiguration((_, config) =>
            {
                config.AddInMemoryCollection(new Dictionary<string, string?>
                {
                    ["Jwt:Secret"] = "test-secret-key-for-unit-tests-0123456789abcdef",
                    ["Jwt:Issuer"] = "ai-project",
                    ["Jwt:Audience"] = "ai-project-frontend"
                });
            });
        });
    }

    [Fact]
    public async Task Register_Login_Me_FullFlow()
    {
        var client = _factory.CreateClient();

        // 1. 注册
        var registerBody = JsonContent.Create(new
        {
            username = "bob",
            email = "bob@example.com",
            password = "password123"
        });
        var registerResp = await client.PostAsync("/api/v1/auth/register", registerBody);
        Assert.Equal(HttpStatusCode.OK, registerResp.StatusCode);
        var registerJson = await registerResp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal(0, registerJson.GetProperty("code").GetInt32());
        Assert.Equal("bob", registerJson.GetProperty("data").GetProperty("username").GetString());

        // 2. 重复注册 -> 409
        var dupResp = await client.PostAsync("/api/v1/auth/register", registerBody);
        Assert.Equal(HttpStatusCode.Conflict, dupResp.StatusCode);
        var dupJson = await dupResp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal(40901, dupJson.GetProperty("code").GetInt32());

        // 3. 登录
        var loginBody = JsonContent.Create(new { username = "bob", password = "password123" });
        var loginResp = await client.PostAsync("/api/v1/auth/login", loginBody);
        Assert.Equal(HttpStatusCode.OK, loginResp.StatusCode);
        var loginJson = await loginResp.Content.ReadFromJsonAsync<JsonElement>();
        var token = loginJson.GetProperty("data").GetProperty("token").GetString();
        Assert.False(string.IsNullOrEmpty(token));

        // 4. 错误密码 -> 401
        var badLogin = JsonContent.Create(new { username = "bob", password = "wrong-pass" });
        var badResp = await client.PostAsync("/api/v1/auth/login", badLogin);
        Assert.Equal(HttpStatusCode.Unauthorized, badResp.StatusCode);
        var badJson = await badResp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal(40101, badJson.GetProperty("code").GetInt32());

        // 5. me（带 Token）
        var meReq = new HttpRequestMessage(HttpMethod.Get, "/api/v1/auth/me");
        meReq.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        var meResp = await client.SendAsync(meReq);
        Assert.Equal(HttpStatusCode.OK, meResp.StatusCode);
        var meJson = await meResp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal("bob", meJson.GetProperty("data").GetProperty("username").GetString());

        // 6. me（无 Token）-> 401
        var noAuthResp = await client.GetAsync("/api/v1/auth/me");
        Assert.Equal(HttpStatusCode.Unauthorized, noAuthResp.StatusCode);
    }

    [Fact]
    public async Task Register_ValidationFails()
    {
        var client = _factory.CreateClient();
        var body = JsonContent.Create(new
        {
            username = "ab",
            email = "not-an-email",
            password = "short"
        });
        var resp = await client.PostAsync("/api/v1/auth/register", body);
        Assert.Equal(HttpStatusCode.BadRequest, resp.StatusCode);
        var json = await resp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal(40001, json.GetProperty("code").GetInt32());
    }

    [Fact]
    public async Task Health_ReturnsUp()
    {
        var client = _factory.CreateClient();
        var resp = await client.GetAsync("/api/v1/health");
        Assert.Equal(HttpStatusCode.OK, resp.StatusCode);
        var json = await resp.Content.ReadFromJsonAsync<JsonElement>();
        Assert.Equal("UP", json.GetProperty("data").GetProperty("status").GetString());
    }
}
