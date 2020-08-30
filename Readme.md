# Simple network library
## installation as a subproject

### Java project
in **build.gradle**
```gradle
dependencies {
    ...
    implementation project(':simple_network_lib')
}

task getLibs {
    doFirst {
        if(!file('libs/').exists())
            mkdir 'libs'
        if(!file('libs/simple_network_lib/').exists())
            mkdir 'libs/simple_network_lib'
        if(!file('libs/simple_network_lib/.git').exists())
            exec {
                commandLine 'git', 'clone', 'https://github.com/StephaneDionisio/simple_network_lib.git', 'libs/simple_network_lib'
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
include ':[MyProject]', ':simple_network_lib'
project(':simple_network_lib').projectDir = new File(settingsDir, 'libs/simple_network_lib')
```
### Android project
in **app/build.gradle**
```gradle
dependencies {
    ...
    implementation project(':simple_network_lib')
}

task getLibs {
    doFirst {
        if(!file('libs/').exists())
            mkdir 'libs'
        if(!file('libs/simple_network_lib/').exists())
            mkdir 'libs/simple_network_lib'
        if(!file('libs/simple_network_lib/.git').exists())
            exec {
                commandLine 'git', 'clone', 'https://github.com/StephaneDionisio/simple_network_lib.git', 'libs/simple_network_lib'
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
include ':app', ':simple_network_lib'
project(':simple_network_lib').projectDir = new File(settingsDir, 'app/libs/simple_network_lib')
```
