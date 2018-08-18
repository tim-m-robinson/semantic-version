package org.tmr

class GitUtil implements Serializable {
  def script

  GitUtil(script) {
    this.script = script
  }

  def reset() {
    // Set up local 'jenkins' git identity
    script.sh "git init"
    script.sh "git config user.email 'jenkins@acme.net'"
    script.sh "git config user.name 'Jenkins'"
  }

  def getTags(boolean force) {
    if ( force ) {
      script.sh "git fetch --tags --force"
    } else {
      script.sh "git fetch --tags"
    }
  }

  def getTags() {
    getTags(false)
  }

  def getTagsCount() {
    script.sh(
      script: "git tag -l | wc -l",
      returnStdout: true
    ).trim()
  }

  def getLatestTagname() {
    script.sh(
      script: 'git tag | tail -1',
      returnStdout: true
    ).trim()
  }

  def getAllCommitComments() {
    script.sh(
      script: 'git log --oneline',
      returnStdout: true
    ).trim()
  }

  def getCommitCommentsSinceTag(String tag) {
    script.sh(
      script: 'git log '+tag+'..HEAD --oneline',
      returnStdout: true
    ).trim()
  }

  def checkout(String branch) {
    script.sh "git checkout ${branch}"
  }

  // Create from current branch. Don't checkout new branch.
  def createBranch(String newBranch) {
    script.sh "git branch ${newBranch}"
  }

  def switchToBranch(String newBranch) {
    script.sh "git checkout ${newBranch}"
  }

  def createBranchCheckout(String newBranch, String tag) {
    script.sh "git checkout -b ${newBranch} ${tag}"
  }

  def addCommitPush(String comment, String branch) {
    script.sh "git commit -am \"${comment}\""
    script.sh "git push origin HEAD:${branch}"
  }

  // Assumes the working copy of the source branch
  def forceMerge(String sourceBranch, String targetBranch) {
    // This first merge is important to avoid merge conflicts.
    script.sh "git merge -s ours origin/${targetBranch}"
    script.sh "git checkout ${targetBranch}"
    script.sh "git merge ${sourceBranch}"
    script.sh "git push origin ${targetBranch}"
  }

  def createTag(String tag) {
    script.sh "git tag -a ${tag} -m '${tag}'"
    script.sh "git push origin ${tag}"
  }

  def createTag(String tag, String message) {
    if (message == null || message.equals("")) {
      createTag(tag)
    } else {
      script.sh "git tag -a '${tag}' -m '${message}'"
      script.sh "git push origin ${tag}"
    }
  }

  def createGitHubRelease(String repository, String tag) {
    // Remove the github url
    def reponame = repository.substring(30);
    if (".git".equals(reponame[-4..-1])) {
      // Remove the .git suffix
      reponame = reponame[0..-5]
    }
    script.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'hts-builduser-github',
                             usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      def command = "curl --user ${script.env.USERNAME}:${script.env.PASSWORD} --request POST \
              https://api.github.com/repos/atosorigin/${reponame}/releases --data\
              \'{\"tag_name\": \"${tag}\",\
              \"target_commitish\": \"develop\",\
              \"name\": \"${tag}\",\
              \"body\": \"${tag}\",\
              \"draft\": false,\
              \"prerelease\": false}\'"

      script.sh command
    }

  }

  def calcNewTagVersionAndPush (String repository, String gitCredentialId) {
    def tagNew, commitList
    def tagCount = getTagsCount()

    if( tagCount == '0' ) {
      commitList = getAllCommitComments()
      tagNew = 'v1.0.0'
    } else {
      // get latest tag
      def tagLatest = getLatestTagname()
      commitList = getCommitCommentsSinceTag(tagLatest)

      if ( commitList.equals('') ) {
        //currentBuild.result = 'ABORTED'
        //error('No changes commited since last tag')
        script.sh echo 'No changes commited since last tag.'
        return
      }

      // Here we calculate the next semantic version
      def currentVersion = SemanticVersion.parse(tagLatest)
      if(currentVersion.isReleaseCandidate()) {
        tagNew = currentVersion.incrementRc().toString()
      } else {
        tagNew = currentVersion.incrementRevision().toString()
      }

    }

    // Create and Push new Tag with list
    // of commit comments
    script.withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: gitCredentialId,
                             usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        sh('git tag -a "'+tagNew+'" -m "'+commitList+'"')
        sh('git push --tags https://${USERNAME}:${PASSWORD}@'+repository)
    }
  }
}
