$env:PATH = 'C:\Users\Lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin;' + [Environment]::GetEnvironmentVariable('PATH','Machine')
cd F:\AIProject\ai\frontend
& 'C:\Users\Lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe' 'F:\AIProject\ai\frontend\node_modules\.pnpm\vite@5.4.21\node_modules\vite\bin\vite.js' --port 3000 --host
