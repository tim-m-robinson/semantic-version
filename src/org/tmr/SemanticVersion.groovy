package org.tmr

class SemanticVersion {
  static final def RELEASE_CANDIDATE = 'RC'
  static final def VERSION_PREFIX = 'v'
  private def int major, minor, revision, candidate = -1

  def SemanticVersion(major, minor, revision, candidate) {
    if (major == null || minor == null || revision == null) {
      throw new IllegalArgumentException("major, minor and revision numbers cannot be null.")
    }
    this.major = major
    this.minor = minor
    this.revision = revision
    if (candidate != null) this.candidate = candidate
  }

  def SemanticVersion(major, minor, revision) {
    this(major, minor, revision, 0)
  }

  static def SemanticVersion parse(String verStr) {
    int _major, _minor, _revision, _candidate
    def matcher = verStr =~ /($VERSION_PREFIX)(\d+)\.(\d+)\.(\d+)\.$RELEASE_CANDIDATE(\d+)/
    if (matcher.size() > 0) {
      _major = Integer.parseInt(matcher[0][2])
      _minor = Integer.parseInt(matcher[0][3])
      _revision = Integer.parseInt(matcher[0][4])
      _candidate = Integer.parseInt(matcher[0][5])
      return new SemanticVersion(_major, _minor, _revision, _candidate)
    } else {
      matcher = verStr =~ /($VERSION_PREFIX)(\d+)\.(\d+)\.(\d+)/
      if (matcher.size() > 0) {
        _major = Integer.parseInt(matcher[0][2])
        _minor = Integer.parseInt(matcher[0][3])
        _revision = Integer.parseInt(matcher[0][4])
        return new SemanticVersion(_major, _minor, _revision)
      } else {
        throw new IllegalArgumentException("Failed to parse: " + verStr)
      }
    }
  }

  def boolean isReleaseCandidate() {
    return (candidate != null && candidate > 0)
  }

  def SemanticVersion incrementMajor() {
    major++
    minor = 0
    revision = 0
    return this
  }

  def SemanticVersion incrementMinor() {
    minor++
    revision = 0
    return this
  }

  def SemanticVersion incrementRevision() {
    revision++
    return this
  }

  def SemanticVersion incrementRc() {
    if (isReleaseCandidate()) {
      candidate++
      return this
    }
    return this
  }

  String toString() {
    StringBuilder sb = new StringBuilder()
    sb.append(VERSION_PREFIX)
            .append(major).append(".")
            .append(minor).append(".")
            .append(revision)
    if (isReleaseCandidate()) {
      sb.append(".").append(RELEASE_CANDIDATE).append(candidate)
    }
    return sb.toString()
  }

}