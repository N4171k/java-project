$url = "https://github.com/official-stockfish/Stockfish/releases/download/sf_15/stockfish-windows-x86-64-avx2.exe"
$output = "resources/stockfish/stockfish_15_x64.exe"

Write-Host "Downloading Stockfish..."
try {
    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($url, $output)
    Write-Host "Download completed successfully!"
} catch {
    Write-Host "Error downloading Stockfish: $_"
} 