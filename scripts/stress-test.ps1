param(
    [int]$clients = 50,
    [int]$cycles = 20
)

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

Write-Host "[stress-test] Compiling Java sources..."
javac src\*.java tests\*.java
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed."
    exit $LASTEXITCODE
}

Write-Host "[stress-test] Running load test with $clients clients and $cycles cycles each..."
# Ensure both source and tests directories are on the classpath so JVM can find LoadTest
java -cp ".;src;tests" LoadTest $clients $cycles
$exitCode = $LASTEXITCODE

if ($exitCode -eq 0) {
    Write-Host "[stress-test] PASS: registry stress test completed successfully."
} else {
    Write-Host "[stress-test] FAIL: stress test returned exit code $exitCode."
}

exit $exitCode
