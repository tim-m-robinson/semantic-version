import org.tmr.SemanticVersion
//
def v = SemanticVersion.parse('v1.0.3')
println(v)
//
v.incrementMajor()
println(v)
//
v.incrementMinor()
println(v)
//
v.incrementMinor()
v.incrementRevision()
println(v)
//
v = SemanticVersion.parse('v1.0.3.RC6')
println(v)
//
v = SemanticVersion.parse('v12.43.173.RC19')
println(v)
v.incrementRc()
println(v)
try {
  v = SemanticVersion.parse('1.0.3')
  println(v)
} catch (Exception e) {
  println('Parse Exception: '+e.getMessage())
}
try {
  v = SemanticVersion.parse('V3.2.1')
  println(v)
} catch (Exception e) {
  println('Parse Exception: '+e.getMessage())
}

