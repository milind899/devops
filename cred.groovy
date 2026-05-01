import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import hudson.util.SecretBytes
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl

def kubeconfigContent = new File('/var/jenkins_home/kubeconfig').bytes
def secretBytes = SecretBytes.fromBytes(kubeconfigContent)

def fileCred = new FileCredentialsImpl(
  CredentialsScope.GLOBAL,
  "k8s-kubeconfig",
  "Auto-injected kubeconfig",
  "config",
  secretBytes
)

def upCred = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  "docker-hub-credentials",
  "Auto-injected docker hub",
  "dummy_user",
  "dummy_password"
)

def store = SystemCredentialsProvider.getInstance().getStore()
store.addCredentials(com.cloudbees.plugins.credentials.domains.Domain.global(), fileCred)
store.addCredentials(com.cloudbees.plugins.credentials.domains.Domain.global(), upCred)
println("Credentials added successfully!")
