// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")


// Play enhancer - this automatically generates getters/setters for public fields
// and rewrites accessors of these fields to use the getters/setters. Remove this
// plugin if you prefer not to have this feature, or disable on a per project
// basis using disablePlugins(PlayEnhancer) in your build.sbt
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")

addSbtPlugin("com.payintech" % "sbt-play-ebean" % "18.04u1")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"