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

if (binding.variables.containsKey('_grails_reencode_init_package_called')) {
    return
}

_grails_reencode_init_package_called = true

includeTargets << grailsScript('_GrailsArgParsing')

binding.reencodeConfig = [
    includeFiles: ['**/*.groovy', '**/*.java', '**/*.gsp', '**/*.jsp', '**/*.xml'],
    excludeFiles: ['target/**/*', '\\.*', '\\.*/**/*'],
    fixReference: false,
    fixReferenceRegexps: ['charset\\s*=\\s*[\\"\\\']{0,1}', 'encoding\\s*=\\s*[\\"\\\']{0,1}'],
    target: 'UTF-8',
    acceptableConfidence: 50
]
        
def config = binding.reencodeConfig

target(_reencodeInit:'') { 
    parseArguments() 
    
    if (argsMap.containsKey('target')) {
        config.target = argsMap['target'].toUpperCase()
    }
    
    if (argsMap.containsKey('confidence')) {
        config.acceptableConfidence = argsMap['confidence'] as int
    }
    
    if (argsMap['include']) {
        config.includeFiles = argsMap['include'].tokenize(',')
    }
      
    
    if (argsMap['exclude']) {
        config.excludeFiles = config.excludeFiles.addAll(argsMap['exclude'].tokenize(','))
    }    
        
        
    if (argsMap.containsKey('fix-ref')) {
        config.fixReference = argsMap['fix-ref']? true: false
        if (config.fixReference && argsMap['fix-ref-regexp']) {
            config.fixReferenceRegexps = argsMap['fix-ref-regexp'].tokenize(',')
        }
    }        
    
}