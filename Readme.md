# Simple network library
## installation as a subproject

### Java project
Create a `libs` directory at the root of your project.  

in **build.gradle**
```gradle
dependencies {
    ...
    implementation project(':libs:simple_network_lib')
}

task getLibs {
    doFirst {
        if(!file('libs/').exists())
            mkdir 'libs'
        if(!file('libs/simple_network_lib/').exists())
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
### Android project
Create a `libs` directory in `app` directory.  

in **app/build.gradle**
```gradle
dependencies {
    ...
    implementation project(':libs:simple_network_lib')
}

task getLibs {
    doFirst {
        if(!file('libs/').exists())
            mkdir 'libs'
        if(!file('libs/simple_network_lib/').exists())
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
preBuild.dependsOn getLibs
```
in **settings.gradle**
```gradle
include ':app', ':libs:simple_network_lib'
project(':libs:simple_network_lib').projectDir = new File(settingsDir, 'app/libs/simple_network_lib')
```