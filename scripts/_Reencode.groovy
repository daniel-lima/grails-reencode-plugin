/*
* Copyright 2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
* @author Daniel Henrique Alves Lima
*/

if (binding.variables.containsKey('_grails_reencode_package_called')) {
   return
}

import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector
// import groovy.util.CharsetToolkit

_grails_reencode_package_called = true

includeTargets << new File("${reencodePluginDir}/scripts/_ReencodeInit.groovy")

target(_reencodeCheck: '') {
    _reencodeInit()
    def baseDir = ant.project.properties.basedir
    _reencode(new File(baseDir), new File("${baseDir}/target"), reencodeConfig, true)    
}


_reencode = {File baseDir, File baseBackupDir, Map config, boolean checkOnly = false -> 
    ant.echo("baseDir ${baseDir}")
    ant.echo("baseBackupDir ${baseBackupDir}")
    ant.echo("config ${config}")
    ant.echo("checkOnly ${checkOnly}")
    
    def backupDir = new File(baseBackupDir, "reenc_${System.currentTimeMillis()}")
    backupDir.mkdirs()
    backupDir.deleteOnExit()
    
    def scanner = ant.fileScanner {
        fileset(dir: baseDir) {
            for (exc in config.excludeFiles) {
                exclude(name: exc)
            }
            
            for (inc in config.includeFiles) {
                include(name: inc)
            }
        }
    }
    
    def fixReferencePatterns = []
    if (config.fixReference) {
        for (regexp in config.fixReferenceRegexps) {
            String r = "(.*\\W*)(${regexp})([a-zA-Z0-9\\-]+)(.*)"
            Pattern pattern = Pattern.compile(r, Pattern.CASE_INSENSITIVE)
            fixReferencePatterns << pattern
        }
    }
    
    CharsetDetector detector = new CharsetDetector()
    for (file in scanner) {
        file.withInputStream {input ->
            input = new BufferedInputStream(input)
            
            def d = detector.setText(input)
            def matches = d.detectAll()
            def possibleEncodings = [:]
            
            for (match in matches) {
                def key = match.name.toUpperCase()
                if (!possibleEncodings.containsKey(key)) {
                    possibleEncodings[key] = match.confidence
                }
            }
            
            def confidence = possibleEncodings[config.target]
            if (confidence == null) {confidence = 0} 
            
            String sourceEncoding = config.target
            if (confidence < config.acceptableConfidence) {
               sourceEncoding = matches[0].name.toUpperCase()
            }
            
            if (config.fixReference) {
                def linesFound = []
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, sourceEncoding))
                String line = null
                while ((line = reader.readLine()) != null) {
                    for (pattern in fixReferencePatterns) {
                        def matcher = pattern.matcher(line)
                        if (matcher.matches()) {
                            String reference = matcher.replaceAll('$3')
                            println reference
                            linesFound << line  
                        }
                    }
                }
                
                println "${file} ${linesFound}"
            }
            
            
                File backupFile = new File(file.absolutePath.replace(baseDir.absolutePath, backupDir.absolutePath))
                //ant.echo("${file} ${backupFile}")
                //ant.copy(file: file, tofile: backupFile, preservelastmodified: true, verbose: true)
            
        }
    }
}