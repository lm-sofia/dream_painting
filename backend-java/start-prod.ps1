# 绘梦工坊 · 启动生产后端（连 Docker PG/Redis + 生成引擎）
# 用法：powershell 执行 ./start-prod.ps1
# 密钥从同目录 .env 读取（没有则用默认值），绝不写死在代码里
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^\s*([A-Z0-9_]+)\s*=\s*(.+)\s*$") {
            [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
            Write-Host "  已加载 $($matches[1])"
        }
    }
} else {
    Write-Host "  未找到 .env（用默认 mock 引擎启动）—— 要真实生成请先复制 .env.example 为 .env"
}

Write-Host "`n>>> 启动后端（Ctrl+C 停止）..."
mvn spring-boot:run
