# Simple network library
## installation as a subproject
Create a `libs` directory at the root of your project.  

in **build.gradle**
```gradle
dependencies {
    ...
    implementation project(':libs:simple_network_lib')
}

task getLibs {
    doFirst {
        mkdir 'libs'
        mkdir 'libs/simple_network_lib'
        if(!file('libs/simple_network_lib/.git').exists())
            exec {
                commandLine 'git', 'clone', 'git@github.com:StephaneDionisio/simple_network_lib.git', 'libs/simple_network_lib'
            }
        else
            exec {
                workingDir 'libs/simple_network_lib'
                commandLine 'git', 'pull'
            }
    }
}
compileJava.dependsOn getLibs
```
in **settings.gradle**
```gradle
include ':[MyProject]', ':libs:simple_network_lib'
project(':libs:simple_network_lib').projectDir = new File(settingsDir, 'libs/simple_network_lib')
```