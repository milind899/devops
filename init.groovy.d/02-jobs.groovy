import jenkins.model.*

def jenkins = Jenkins.get()

def jobs = [
    [name: 'Activity-1-CICD-Pipeline', script: 'activity-1-cicd-pipeline/Jenkinsfile'],
    [name: 'Activity-2-BlueGreen',     script: 'activity-2-blue-green/Jenkinsfile'],
    [name: 'Activity-3-DevSecOps',     script: 'activity-3-devsecops/Jenkinsfile']
]

jobs.each { jobDef ->
    if (!jenkins.getItem(jobDef.name)) {
        def xml = """<?xml version='1.1' encoding='UTF-8'?>
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
    <scriptPath>${jobDef.script}</scriptPath>
    <lightweight>false</lightweight>
  </definition>
  <disabled>false</disabled>
</flow-definition>"""
        def stream = new ByteArrayInputStream(xml.bytes)
        jenkins.createProjectFromXML(jobDef.name, stream)
        println "Created job: ${jobDef.name}"
    } else {
        println "Job already exists: ${jobDef.name}"
    }
}

jenkins.save()
