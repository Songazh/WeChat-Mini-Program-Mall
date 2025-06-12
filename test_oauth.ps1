# OAuth2.0测试脚本

# 测试令牌端点
$tokenUrl = "http://localhost:8080/auth/oauth/token"
$tokenBody = @{
    grant_type = "authorization_code"
    code = "AUTH_815a30aaa5764c2291ea45c850607a55"
    client_id = "admin_client"
    client_secret = "admin_secret_2024"
} | ConvertTo-Json

$tokenHeaders = @{
    "Content-Type" = "application/json"
}

Write-Host "测试OAuth2.0令牌端点..."
try {
    $tokenResponse = Invoke-RestMethod -Uri $tokenUrl -Method POST -Body $tokenBody -Headers $tokenHeaders
    Write-Host "令牌获取成功!"
    $tokenResponse | ConvertTo-Json -Depth 3
} catch {
    Write-Host "令牌获取失败: $($_.Exception.Message)"
} 