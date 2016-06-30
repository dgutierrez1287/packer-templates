#!/usr/bin/env groovy
@Grapes([
    @Grab('org.codehaus.groovy:http-builder'),
])

import groovy.transform.Field
import groovyx.net.http.HTTPBuilder
import groovy.json.*
import java.security.MessageDigest
import java.io.*
import groovy.util.*

// Functions //
def checkForAtlasBox(boxName, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.get(path: "/api/v1/user/${atlasUser}",
    query: 'access_token': atlasToken
  ) { resp, json ->
      if ( resp.statusLine.getStatusCode() == 200 ){

        def jsonSlurper = new JsonSlurper()
        def boxes = jsonSlurper.parseText(json.toString()).boxes.name

        if ( boxes.contains(boxName) ) {
          return true
        }
        else {
          return false
        }
      }
      else {
        println "Error: couldnt't communicate with Hashicorp Atlas"
        System.exit(1)
      }
   }
}

def createAtlasBox(boxName, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.post(path: "/api/v1/boxes",
    body: [
      'box[name]': boxName,
      'box[is_private]': 'false',
      'access_token': atlasToken
    ]
  ){ resp, json ->
    if (resp.statusLine.getStatusCode() == 200){
      println "created box on atlas for ${boxName}"
    }
    else {
      println "Error: couldn't create the box on Hashicorp Atlas"
      System.exit(1)
    }
  }
}

def checkForAtlasVersion(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.get(path: "/api/v1/user/${atlasUser}",
    query: 'access_token': atlasToken
  ) { resp, json ->
      if (resp.statusLine.getStatusCode() == 200){

      def jsonSlurper = new JsonSlurper()
      def boxes = jsonSlurper.parseText(json.toString()).boxes

      for (box in boxes){
        if (box.name == boxName) {
          versions = box.versions.version

          if (versions.contains(publicVersion)){
            return true
          }
          else {
            return false
          }
        }
      }
    }
    else {
      println "Error: couldnt't communicate with Hashicorp Atlas"
      System.exit(1)
    }
  }
}

def createAtlasVersion(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.post(path: "/api/v1/box/${atlasUserName}/${boxName}/versions",
    body: [
      'version[version]': publicVersion,
      'version[description]': publicDescription,
      'access_token': atlasToken
    ]
  ){ resp, json ->
    if (resp.statusLine.getStatusCode() == 200){
      println "created version ${publicVersion} on atlas for box ${boxName}"
    }
    else {
      println "Error: couldn't create the box on Hashicorp Atlas"
      System.exit(1)
    }
  }
}

def createAtlasProvider(boxName, providerType, publicVersion, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.post(path: "/api/v1/box/${atlasUserName}/${boxName}/version/${publicVersion}/providers",
    body: [
      'provider[name]': providerType,
      'access_token': atlasToken
    ]
  ) { resp, json ->
    if (resp.statusLine.getStatusCode() == 200) {
      println "created atlas provider of type ${providerType} for box ${boxName} and version ${publicVersion}"
    }
    else {
      println "Error: couldn't create atlas ${providerType} provder"
      System.exit(1)
    }
  }
}

def getAtlasUploadPath(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.get(path: "/api/v1/box/${atlasUserName}/${boxName}/version/${publicVersion}/provider/virtualbox/upload",
    query: 'access_token': atlasToken
  ) { resp, json ->
    if (resp.statusLine.getStatusCode() == 200){
      def jsonSlurper = new JsonSlurper()
      def uploadPath = jsonSlurper.parseText(json.toString()).token

      return uploadToken
    }
    else {
      println "Error: couldn't get atlas upload path for provider: ${providerType}, version: ${publicVersion}"
      System.exit(1)
    }
  }
}

def uploadBoxToAtlas(boxName, uploadToken, atlasToken) {

  def http = new HTTPBuilder("https://binstore.hashicorp.com/")
  def resp = http.put(path: uploadToken,
    body: "${boxName}.box",
    query: 'access_token': atlasToken
  ) { resp, json ->
    if (resp.statusLine.getStatusCode() == 200) {
      println "box ${boxName} uploaded to atlas"
    }
    else {
      println "Error: couldn't upload the box ${boxName} to atlas"
      System.exit(1)
    }
  }
}

def atlasReleaseBox(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName) {

  def http = new HTTPBuilder(atlasBaseUrl)
  def resp = http.put(path: "/api/v1/box/${atlasUserName}/${boxName}/version/${publicVersion}/release",
    body: 'access_token': atlasToken
  ) { resp, json ->
    if (resp.statusLine.getStatusCode() == 200) {
      println "successfully released atlas version ${publicVersion} for ${boxName}"
    }
    else {
      println "Error: couldn't relase version ${publicVersion} for ${boxName}"
      System.exit(1)
    }
  }
}

def buildBox(osName, osVersion, osArch, buildType, packerTemplate) {

  def buildCmd = [
    'bash',
    '-c',
    'export PATH="$HOME/bin/packer:/usr/local/bin:$PATH"\n'+
    'export PACKER_CACHE="$HOME/.packer_cache"\n'+
    'export PACKER_CACHE_DIR="$HOME/.packer_cache"\n'+
    "~/bin/packer/packer build -force ${buildType}/${osVersion}/${osArch}/${packerTemplate}.json"
  ]

  println " "
  println " "
  println "Starting build for the ${buildType} box"

  Process p = buildCmd.execute()
  p.consumeProcessOutput(System.out, System.err)
  p.waitFor()

  if (p.exitValue() != 0) {
    println "Error there was an issue building the box for buildType ${buildType}"
    System.exit(1)
  }

  println " "
  println " "
}

////

// MAIN() //
def cli = new CliBuilder(usage:'build.groovy [options]')
cli.with {
  h longOpt:'help', 'Show usage information'
  u longOpt:'user', args:1, argName:'atlasUserName', 'Username for Atlas'
  b longOpt:'boxName', args:1, argName:'boxName', 'The name of the box'
  p longOpt:'providerType', args:1, argName:'provider', 'The provider type for the Vagrant box'
  o longOpt:'osName', args:1, argName:'osName', 'The OS name for the box'
  v longOpt:'osVersion', args:1, argName:'osVersion', 'The OS version for the box'
  a longOpt:'osArch', args:1, argName:'osArch', 'The OS arch for the box'
  f longOpt:'packerTemplate', args:1, argName:'packerTemplate', 'The name of the packer template'
}

def options = cli.parse(args)

if (options.h) {
  cli.usage()
  System.exit(0)
}

// create variables from cli options
def atlasUser = options.u
def boxName = options.b
def providerType = options.p
def osName = options.o
def osVersion = options.v
def osArch = options.a
def packerTemplate = options.f

// read build paramters
def env = System.getenv()
def publicVersion = env['PUBLIC_VERSION']
def publicDescription = env['PUBLIC_VERSION_DESCRIPTION']

// read atlas token from credentials plugin
def atlasToken = env['ATLAS_TOKEN']

// the base atlas URL
def atlasBaseUrl = "https://atlas.hashicorp.com"

//check for atlas box .. if false create atlas box
atlasBoxExists = checkForAtlasBox(boxName, atlasToken, atlasBaseUrl, atlasUserName)

if (!atlasBoxExists) {
  println "atlas box doesn't exist, creating the box"
  createAtlasBox(boxName, atlasToken, atlasBaseUrl, atlasUserName)
}
else {
  println "atlas box already exists"
}

//if box existed check for atlas version .. if false create atlas version
atlasVersionExists = checkForAtlasVersion(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName)

if (!atlasVersionExists) {
  println "atlas version doesn't exist, creating the version"
  createAtlasVersion(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName)
}
// error since the script doesnt support multiple providers
else {
  println "Error: atlas version already exists"
  System.exit(1)
}

//create provider for atlas box
println "Creating atlas provider"
createAtlasProvider(boxName, providerType, publicVersion, atlasToken, atlasBaseUrl, atlasUserName)

//build box
buildBox(osName, osVersion, osArch, "public", packerTemplate)

//get upload token
println "Getting the upload path for atlas"
uploadToken = getAtlasUploadPath(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName)
println "upload path for atlas: ${uploadToken}"

//upload box to atlas
println "Uploading box to atlas"
uploadBoxToAtlas(boxName, uploadToken, atlasToken)

//release atlas box
println "Atlas box released for version: ${publicVersion}"
atlasReleaseBox(boxName, publicVersion, atlasToken, atlasBaseUrl, atlasUserName)

System.exit(0)
