import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl

def jenkins = Jenkins.get()

def kubeFile = new File('/var/jenkins_casc/jenkins-kubeconfig')
if (kubeFile.exists()) {
    def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    def domain = Domain.global()

    def existing = store.getCredentials(domain).find { it.id == 'k8s-kubeconfig' }
    if (!existing) {
        def secretBytes = SecretBytes.fromBytes(kubeFile.bytes)
        def cred = new FileCredentialsImpl(
            CredentialsScope.GLOBAL,
            'k8s-kubeconfig',
            'Kubernetes kubeconfig',
            'kubeconfig',
            secretBytes
        )
        store.addCredentials(domain, cred)
        println 'Created k8s-kubeconfig credential'
    } else {
        println 'k8s-kubeconfig credential already exists'
    }
}

jenkins.save()
