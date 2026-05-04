param(
    [string]$JenkinsUrl = "http://localhost:8080",
    [string]$User = "admin",
    [string]$Password = "admin"
)

Write-Host "Waiting for Jenkins to be ready..."
$maxWait = 120
$waited = 0
do {
    Start-Sleep -Seconds 5
    $waited += 5
    try {
        $response = Invoke-WebRequest -Uri "$JenkinsUrl/login" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        if ($response.StatusCode -eq 200) { break }
    } catch {}
    Write-Host "  Still waiting... ($waited s)"
} while ($waited -lt $maxWait)

Write-Host "Jenkins is up. Getting crumb..."
$pair = "$User`:$Password"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [Convert]::ToBase64String($bytes)
$headers = @{ Authorization = "Basic $base64" }

$crumbResp = Invoke-RestMethod -Uri "$JenkinsUrl/crumbIssuer/api/json" -Headers $headers
$headers[$crumbResp.crumbRequestField] = $crumbResp.crumb

$jobs = @(
    @{
        name   = "Activity-1-CICD-Pipeline"
        script = "activity-1-cicd-pipeline/Jenkinsfile"
    },
    @{
        name   = "Activity-2-BlueGreen"
        script = "activity-2-blue-green/Jenkinsfile"
    },
    @{
        name   = "Activity-3-DevSecOps"
        script = "activity-3-devsecops/Jenkinsfile"
    }
)

foreach ($job in $jobs) {
    $xml = @"
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job">
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/milind899/devops.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>$($job.script)</scriptPath>
    <lightweight>false</lightweight>
  </definition>
  <disabled>false</disabled>
</flow-definition>
"@

    $checkUrl = "$JenkinsUrl/job/$([Uri]::EscapeDataString($job.name))/api/json"
    $exists = $false
    try {
        Invoke-RestMethod -Uri $checkUrl -Headers $headers -ErrorAction Stop | Out-Null
        $exists = $true
    } catch {}

    if ($exists) {
        Write-Host "  Updating job: $($job.name)"
        $url = "$JenkinsUrl/job/$([Uri]::EscapeDataString($job.name))/config.xml"
        Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $xml -ContentType "application/xml" | Out-Null
    } else {
        Write-Host "  Creating job: $($job.name)"
        $url = "$JenkinsUrl/createItem?name=$([Uri]::EscapeDataString($job.name))"
        Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $xml -ContentType "application/xml" | Out-Null
    }
    Write-Host "  Done: $($job.name)" -ForegroundColor Green
}

Write-Host ""
Write-Host "All jobs created! Open $JenkinsUrl" -ForegroundColor Cyan
