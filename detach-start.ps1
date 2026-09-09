﻿Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;

public static class ProcessHelper
{
    [StructLayout(LayoutKind.Sequential)]
    public struct STARTUPINFO
    {
        public int cb;
        public string lpReserved;
        public string lpDesktop;
        public string lpTitle;
        public uint dwX;
        public uint dwY;
        public uint dwXSize;
        public uint dwYSize;
        public uint dwXCountChars;
        public uint dwYCountChars;
        public uint dwFillAttribute;
        public uint dwFlags;
        public short wShowWindow;
        public short cbReserved2;
        public IntPtr lpReserved2;
        public IntPtr hStdInput;
        public IntPtr hStdOutput;
        public IntPtr hStdError;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct PROCESS_INFORMATION
    {
        public IntPtr hProcess;
        public IntPtr hThread;
        public uint dwProcessId;
        public uint dwThreadId;
    }

    [DllImport("kernel32.dll", SetLastError = true, CharSet = CharSet.Auto)]
    public static extern bool CreateProcess(
        string lpApplicationName,
        string lpCommandLine,
        IntPtr lpProcessAttributes,
        IntPtr lpThreadAttributes,
        bool bInheritHandles,
        uint dwCreationFlags,
        IntPtr lpEnvironment,
        string lpCurrentDirectory,
        ref STARTUPINFO lpStartupInfo,
        out PROCESS_INFORMATION lpProcessInformation);

    public const uint CREATE_BREAKAWAY_FROM_JOB = 0x01000000;
    public const uint CREATE_NO_WINDOW = 0x08000000;
    public const uint DETACHED_PROCESS = 0x00000008;

    public static uint StartDetached(string exePath, string args, string workDir)
    {
        STARTUPINFO si = new STARTUPINFO();
        si.cb = Marshal.SizeOf(typeof(STARTUPINFO));
        PROCESS_INFORMATION pi;
        string cmdLine = "\"" + exePath + "\" " + args;
        bool ok = CreateProcess(null, cmdLine, IntPtr.Zero, IntPtr.Zero, false,
            CREATE_BREAKAWAY_FROM_JOB | CREATE_NO_WINDOW, IntPtr.Zero, workDir, ref si, out pi);
        if (ok) return pi.dwProcessId;
        return 0;
    }
}
"@

Get-Process java,node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 2
Remove-Item "D:\software\huaw-demo\gaokao-volunteer\backend\logs" -Recurse -Force -ErrorAction SilentlyContinue

$jar = "D:\software\huaw-demo\gaokao-volunteer\backend\target\gaokao-volunteer-1.0.0.jar"
$wd = "D:\software\huaw-demo\gaokao-volunteer\backend"
$java = "C:\Program Files\Java\jdk-17\bin\java.exe"

$backendPid = [ProcessHelper]::StartDetached($java, "-jar `"$jar`"", $wd)
"Backend PID=$backendPid"

Start-Sleep 3

$npm = "npm.cmd"
$frontendDir = "D:\software\huaw-demo\gaokao-volunteer\frontend"
$frontendPid = [ProcessHelper]::StartDetached("cmd.exe", "/c npm run dev", $frontendDir)
"Frontend PID=$frontendPid"

Start-Sleep 40
"---check---"
Get-Process java -ErrorAction SilentlyContinue | Select-Object Id, StartTime
Get-Process node -ErrorAction SilentlyContinue | Select-Object Id, StartTime
netstat -ano | Select-String "LISTENING" | Select-String ":8080|:3000"
"---API---"
try { $r = Invoke-RestMethod -Uri "http://localhost:8080/api/public/colleges?page=0&size=1" -TimeoutSec 5; "Backend OK total=$($r.data.totalElements)" } catch { "Backend err: $($_.Exception.Message)" }
try { $w = Invoke-WebRequest "http://localhost:3000" -TimeoutSec 5 -UseBasicParsing; "Frontend OK status=$($w.StatusCode)" } catch { "Frontend err: $($_.Exception.Message)" }