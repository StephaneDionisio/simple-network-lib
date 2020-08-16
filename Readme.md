# Simple network library
## installation as a subproject

### Java project
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

tasks.matching { it.name != 'getLibs' }.all { Task task ->
    task.dependsOn getLibs
}
```

in **settings.gradle**
```gradle
include ':[MyProject]', ':libs:simple_network_lib'
project(':libs:simple_network_lib').projectDir = new File(settingsDir, 'libs/simple_network_lib')
```
### Android project
in **/build.gradle**
```gradle
dependencies {
    ...
    implementation project(':libs:simple_network_lib')
}

task getLibs {
    doFirst {
        if(!file('app/libs/').exists())
            mkdir 'app/libs'
        if(!file('app/libs/simple_network_lib/').exists())
            mkdir 'app/libs/simple_network_lib'
        if(!file('app/libs/simple_network_lib/.git').exists())
            exec {
                commandLine 'git', 'clone', 'git@github.com:StephaneDionisio/simple_network_lib.git', 'app/libs/simple_network_lib'
            }
        else
            exec {
                workingDir 'app/libs/simple_network_lib'
                commandLine 'git', 'pull'
            }
    }
}

tasks.matching { it.name != 'getLibs' }.all { Task task ->
    task.dependsOn getLibs
}
```

in **settings.gradle**
```gradle
include ':app', ':libs:simple_network_lib'
project(':libs:simple_network_lib').projectDir = new File(settingsDir, 'app/libs/simple_network_lib')
```
