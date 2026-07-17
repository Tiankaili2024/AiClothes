$token = Get-Content F:\AIProject\ai\.token -Raw
$genBody = @{ userInput = '通勤' } | ConvertTo-Json -Compress
$r = Invoke-WebRequest -Uri 'http://localhost:8080/api/outfit/generate' -Method POST -Body $genBody -ContentType 'application/json' -Headers @{ Authorization = 'Bearer ' + $token } -UseBasicParsing -TimeoutSec 300
$r.Content | Set-Content F:\AIProject\ai\last_gen.json
